/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.rcp;

import java.util.ArrayList;

import org.eclipse.recommenders.snipmatch.core.ArgumentMatchNode;
import org.eclipse.recommenders.snipmatch.core.EffectMatchNode;
import org.eclipse.recommenders.snipmatch.core.MatchEnvironment;
import org.eclipse.recommenders.snipmatch.core.MatchNode;
import org.eclipse.recommenders.snipmatch.web.ISearchListener;
import org.eclipse.recommenders.snipmatch.web.ISendFeedbackListener;
import org.eclipse.recommenders.snipmatch.web.MatchClient;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;



/**
 * This is the main search interface.
 */
public class SearchBox {

	private int idealWidth;
	private int maxMatches;
	private Color queryBackColor;
	private Color queryDisabledBackColor;
	private Color matchSelectBackColor;
	private Color matchKeywordForeColor;
	private Color matchArgForeColor;
	private Color matchBlankArgForeColor;
	private Font searchFont;
	private Cursor arrowCursor;
	private Cursor handCursor;
	
	private MatchClient client;
	private MatchEnvironment env;
	private Shell shell;
	private StyledText queryText;
	private ArrayList<MatchNode> matches;
	private ArrayList<StyledText> matchTexts;
	private ArrayList<CompleteMatchThread> completeMatchThreads;
	private Shell buttonBar;
	private Shell refineDialog;
	private boolean noResultsYet;
	private int selection;
	private boolean selectionConfirmed;
	private CloseOnIgnoreListener ignoredListener;
	private boolean ignoreTextChange;
	private EffectMatchNode refinedMatch;
	
	public SearchBox(MatchClient client) {
		
		this.client = client;
	}

	public void show(final String envName) {

		idealWidth = 360;
		maxMatches = 10;
		queryBackColor = new Color(
				PlatformUI.getWorkbench().getDisplay(), 255, 255, 200);
		queryDisabledBackColor =
				PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		matchSelectBackColor = new Color(
				PlatformUI.getWorkbench().getDisplay(), 220, 220, 255);
		matchKeywordForeColor =
				PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK);
		matchArgForeColor =
				PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLUE);
		matchBlankArgForeColor = new Color(
				PlatformUI.getWorkbench().getDisplay(), 125, 150, 255);
		searchFont = new Font(
				PlatformUI.getWorkbench().getDisplay(), "Arial", 10, SWT.NORMAL);
		arrowCursor = new Cursor(
				PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_ARROW);
		handCursor = new Cursor(
				PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_HAND);
		
		if (envName.equals("javasnippet")) {
			
			env = new JavaSnippetMatchEnvironment();
			
			if (((JavaSnippetMatchEnvironment)env).getProject() == null) {
				
				MessageBox popup = new MessageBox(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						SWT.ICON_ERROR | SWT.OK | SWT.APPLICATION_MODAL);
				
				popup.setText("SnipMatch");
				popup.setMessage("Searching from independent source files is not" +
						"supported at this time. Please make sure the source file " +
						"is part of an open Java project.");
				popup.open();
				
				return;
			}
		}

		if (shell == null || shell.isDisposed()) {

			shell = new Shell(PlatformUI.getWorkbench().
					getActiveWorkbenchWindow().getShell(),
					SWT.NO_TRIM | SWT.NO_FOCUS | SWT.NO_BACKGROUND);
			
			shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
			
			{
				queryText = new StyledText(shell, SWT.BORDER);
				queryText.setBackground(queryBackColor);
				queryText.setMargins(8, 6, 8, 6);
				queryText.setFont(searchFont);
				queryText.setSize(idealWidth,
						queryText.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				
				queryText.addModifyListener(new ModifyListener() {
					
					@Override
					public void modifyText(ModifyEvent e) {
						
						handleTyping();
					}
				});
				
				queryText.addKeyListener(new KeyListener() {
					
					@Override
					public void keyReleased(KeyEvent e) {}
					
					@Override
					public void keyPressed(KeyEvent e) {
						
						switch (e.keyCode) {
						
						// If Enter is pressed...
						case '\r':
							
							// This signals that the user wishes to insert the currently highlighted result.
							selectionConfirmed = true;
							
							if (!client.isWorking() && completeMatchThreads.isEmpty()) {

								if (selection != -1) {
									
									EffectMatchNode match = (EffectMatchNode) matches.get(selection);
									
									/* If the user hit Ctrl-Enter, or if the selected result is incomplete,
									 * show the refinement dialog.
									 */
									if (!match.isComplete() || (e.stateMask & SWT.CTRL) != 0) {
										showRefinementDialog();
									}
									else shell.close();
								}
								else shell.close();
							}
	
							e.doit = false;
							break;
							
						case '\t':
							
							// If tab is pressed, then tab-complete the search query to the next argument.
	
							if (selection != -1) {
								
								EffectMatchNode match = (EffectMatchNode) matches.get(selection);
								
								ArrayList<int[]> argRanges = new ArrayList<int[]>();
								ArrayList<int[]> blankArgRanges = new ArrayList<int[]>();
						
								String matchString = buildMatchString(match, argRanges, blankArgRanges, false, 0);

								String newQuery;
								
								if (!matchString.toLowerCase().
									startsWith(queryText.getText().toLowerCase())) {
			
									e.doit = false;
									break;
								}
									
								int nextStop = queryText.getCharCount();
								boolean changed = false;
								
								for (int[] argRange : argRanges) {
									
									if (argRange[0] > nextStop) {
										nextStop = argRange[0];
										changed = true;
										break;
									}

									if (argRange[1] == 0) break;
								}
								
								if (match.isComplete() && nextStop == queryText.getCharCount() &&
										nextStop < matchString.length()) {
									nextStop = matchString.length();
									changed = true;
								}
								
								if (!changed) {
									e.doit = false;
									break;
								}
							
								newQuery = matchString.substring(0, nextStop);
								
								queryText.setText(newQuery);
								queryText.setCaretOffset(queryText.getCharCount());
							}

							e.doit = false;
							break;
							
						case SWT.ARROW_UP:
							selectMatchByOffset(-1);
							e.doit = false;
							break;
							
						case SWT.ARROW_DOWN:
							selectMatchByOffset(1);
							e.doit = false;
							break;
						}
					}
				});

				ignoredListener = new CloseOnIgnoreListener(shell);
				queryText.addFocusListener(ignoredListener);
			}
		}
		
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent evt) {
	
				cancelThreads();
				
				env.reset(); // Reset all code changes.
				
				/* If the search box was closed by confirming a selection rather than canceling it,
				 * then the selected result should be re-applied fully (not just the preview).
				 */
				if (selectionConfirmed && selection != -1) {
	
					try {
						((JavaSnippetMatchEnvironment) env).
						applyMatch(matches.get(selection), true, getTotalHeight());
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				if (selection != -1) sendUsageData();
			}
		});

		shell.pack();
		
		if (env instanceof JavaSnippetMatchEnvironment) {
			
			// Set the initial position of the search box to be right below the cursor.
			Point anchor = ((JavaSnippetMatchEnvironment)env).getSearchBoxAnchor(getTotalHeight());
			shell.setLocation(anchor.x, anchor.y);
		}
		
		shell.open();

		matches = new ArrayList<MatchNode>();
		matchTexts = new ArrayList<StyledText>();
		completeMatchThreads = new ArrayList<CompleteMatchThread>();
		noResultsYet = false;
		selection = -1;
		selectionConfirmed = false;
		
		/* Create all the search result shells ahead of time, and just hide/reveal them as needed.
		 * This prevents lag during search.
		 */
		
		for (int i = 0; i < maxMatches; i++) {

			Shell matchShell = new Shell(shell, SWT.NO_TRIM);

			final StyledText matchText = new StyledText(matchShell,
					SWT.BORDER | SWT.MULTI | SWT.WRAP);
			matchText.setMarginColor(matchText.getBackground());
			matchText.setMargins(5, 3, 5, 3);
			matchText.setFont(searchFont);
			matchText.setEditable(false);
			matchText.setCursor(arrowCursor);
			
			matchText.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseUp(MouseEvent e) {}
				
				@Override
				public void mouseDown(MouseEvent e) {
					
					selectMatchByOffset(matchTexts.indexOf(matchText) - selection);
					queryText.setFocus();
				}
				
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					
					selectMatchByOffset(matchTexts.indexOf(matchText) - selection);
					selectionConfirmed = true;
	
					if (!client.isWorking() && completeMatchThreads.isEmpty()) {
	
						if (selection != -1) {
							
							EffectMatchNode match = (EffectMatchNode) matches.get(selection);
							
							if (!match.isComplete()) {
								showRefinementDialog();
							}
							else shell.close();
						}
						else shell.close();
					}
				}
			});
			
			matchTexts.add(matchText);
			
			matchText.addFocusListener(ignoredListener);
			matchShell.addFocusListener(ignoredListener);
			
			matchText.setSize(new Point(0, 0));
			matchShell.setSize(new Point(0, 0));

			matchShell.open();
			matchShell.setFocus();
		}
		
		shell.setFocus();
	}
	
	private void sendUsageData() {

		ISendFeedbackListener listener = new ISendFeedbackListener() {
			@Override
			public void sendFeedbackSucceeded() {}
			@Override
			public void sendFeedbackFailed(final String error) {}
		};

		if (!client.isWorking())
			client.startSendFeedback(queryText.getText(), matches.get(selection),
					null, 1, false, true, false, SnipMatchPlugin.getClientId(), selectionConfirmed, listener);
	}
	
	/**
	 * Gets the height of the entire search interface (query box + spacing + search results).
	 * @return
	 */
	private int getTotalHeight() {
		
		int totalHeight = shell.getSize().y;
		if (matches != null && matches.size() != 0 && !matchTexts.get(0).isDisposed())
			totalHeight += matches.size() * matchTexts.get(0).getSize().y + 3;
		
		return totalHeight;
	}
	
	/**
	 * Called when the contents of the query box have changed.
	 */
	private void handleTyping() {
		
		final String query = queryText.getText();

		/* If the query is empty, then reset all changes,
		 * cancel all on-going searches, and reset the search box position.
		 */
		if (query.isEmpty()) {

			clearMatches();
			cancelThreads();
			env.reset();

			shell.setLocation(shell.getLocation().x,
					((JavaSnippetMatchEnvironment) env).getSearchBoxAnchor(getTotalHeight()).y);
	
			return;
		}
		
		/* If the query text field is not editable, then it means this was a programmatically
		 * invoked status message change.
		 */
		if (!queryText.getEditable()) return;
		
		/* If an invalid character was entered, ignore it, fix the query, and ignore the next
		 * text change as well, because it will be due to the fix.
		 */
		if (queryText.getText().contains("\t") || queryText.getText().contains("\r") ||
				queryText.getText().contains("\n")) {
			
			ignoreTextChange = true;
			
			queryText.setText(queryText.getText().replace("\t", "").replace("\r", "").
					replace("\n", ""));
			
			queryText.setSelectionRange(queryText.getCharCount(), 0);

			return;
		}

		if (ignoreTextChange) {
			ignoreTextChange = false;
			return;
		}
		
		// Set the tool tip to the query, in case the query doesn't fit in the search box.
		queryText.setToolTipText(query);

		// Just in case? Don't remember what this was for, but it doesn't hurt...
		if (query.endsWith(System.getProperty("line.separator"))) return;

		client.startSearch(query, env, new ISearchListener() {
			
			@Override
			public void searchSucceeded() {

				// Wait for matches to be completed.
				while (!completeMatchThreads.isEmpty()) {
					/* This is bogus code that doesn't do anything, but SOMETHING has
					 * to be here for this loop to terminate eventually. No idea why... */
					try { Thread.sleep(1); }
					catch (InterruptedException e) {}
				}
	
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {

						// Search finished without any results.
						if (noResultsYet) {
							
							env.reset();
							clearMatches();
							shell.setLocation(shell.getLocation().x,
									((JavaSnippetMatchEnvironment) env).getSearchBoxAnchor(getTotalHeight()).y);
						}
						else {

							sortMatches();
							displayMatches();
				
							// Select the first result.
							if (matches.size() > 0) selectMatchByOffset(1);
			
							/* If the user already pressed Enter before the search finished,
							 * then select and apply the first result automatically.
							 */
							if (selectionConfirmed) {
	
								if (selection != -1) {
									
									EffectMatchNode match = (EffectMatchNode) matches.get(selection);
									
									if (match.isComplete()) shell.close();
									else showRefinementDialog();
								}
								else shell.close();
							}
						}
					}
				});
			}

			@Override
			public void searchFailed(final String error) {
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {

						MessageBox popup = new MessageBox(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								SWT.ICON_ERROR | SWT.OK | SWT.APPLICATION_MODAL);
						
						popup.setText("SnipMatch");
						popup.setMessage(error);
						popup.open();
					}
				});
			}
			
			@Override
			public void matchFound(final MatchNode match) {
				
				// When a match is found, generate completions from it, and add them.

				final CompleteMatchThread matchCompleter = new CompleteMatchThread(env, match);
				
				ICompleteMatchListener listener = new ICompleteMatchListener() {
					
					@Override
					public void completionFound(MatchNode match) {

						addMatch(match);
					}
					
					@Override
					public void completionFinished() {

						completeMatchThreads.remove(matchCompleter);
					}
				};
				
				matchCompleter.setListener(listener);
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(matchCompleter);
				completeMatchThreads.add(matchCompleter);
			}
		});

		noResultsYet = true;
	}
	
	/**
	 * Puts all the empty matches (no arguments filled in) on the bottom.
	 */
	private void sortMatches() {
		
		int place = 0;
		
		for (int i = 0; i < matches.size(); i++) {
			
			EffectMatchNode match = (EffectMatchNode) matches.get(place);
			
			if (match.isEmpty()) {
				matches.remove(match);
				matches.add(match);
			}
			else place++;
		}
	}
	
	private void cancelThreads() {

		client.cancelWork();
		
		for (CompleteMatchThread completeMatchThread : completeMatchThreads) {
			completeMatchThread.cancel();
		}
		
		completeMatchThreads.clear();
	}
	
	/**
	 * Adds a completed match result to to result list.
	 * @param match
	 */
	private void addMatch(MatchNode match) {
		
		// If this is the first result of a search, then clear everything from the previous search.
		if (noResultsYet) {
			clearMatches();
			noResultsYet = false;
		}

		// Prevent duplicates or extras.
		if (matches.contains(match) || matches.size() >= maxMatches) return;

		matches.add(match);
	}
	
	/**
	 * Updates the visual result listing based on the search results.
	 */
	private void displayMatches() {
		
		for (int i = 0; i < matches.size(); i++) {
		
			MatchNode match = matches.get(i);
			StyledText matchText = matchTexts.get(i);
			if (matchText.isDisposed()) continue;
			Shell matchShell = matchText.getShell();

			ArrayList<int[]> argRanges = new ArrayList<int[]>();
			ArrayList<int[]> blankArgRanges = new ArrayList<int[]>();
			String dispStr = buildMatchString(match, argRanges, blankArgRanges, true, 0);
			
			matchText.setText(dispStr);
	
			matchText.setStyleRange(new StyleRange(0, matchText.getText().length(),
					matchKeywordForeColor, matchText.getBackground()));
			
			for (int[] argRange : argRanges) {
	
				matchText.setStyleRange(new StyleRange(argRange[0], argRange[1],
						matchArgForeColor, matchText.getBackground(), SWT.BOLD));
			}
			
			for (int[] blankArgRange : blankArgRanges) {
	
				matchText.setStyleRange(new StyleRange(blankArgRange[0], blankArgRange[1],
						matchBlankArgForeColor, matchText.getBackground(), SWT.BOLD));
			}
	
			matchText.pack();
			
			if (matchText.getSize().x > queryText.getSize().x)
				matchText.setSize(queryText.getSize().x, matchText.getSize().y * 2 -
						matchText.getTopMargin() - matchText.getBottomMargin() -
						matchText.getBorderWidth() * 2);
			else matchText.setSize(queryText.getSize().x, matchText.getSize().y);
			
			matchShell.pack();
			matchShell.setSize(shell.getSize().x, matchText.getSize().y);
			
			if (i == 0) {
				matchShell.setLocation(shell.getLocation().x,
						shell.getLocation().y + shell.getSize().y + 3);
			}
			else {
				Composite last = matchTexts.get(i - 1).getParent();
				matchShell.setLocation(last.getLocation().x,
						last.getLocation().y + last.getSize().y - 1);
			}
			
			matchShell.setVisible(true);
			
			queryText.setFocus();
		}
	}
	
	/**
	 * Gets a string representation of a match, along with some other information.
	 * @param match The match.
	 * @param argRanges A list to be filled with the ranges of non-empty arguments.
	 * @param blankArgRanges A list to be filled with the ranges of empty arguments.
	 * @param showBlanks Whether or not to show place-holders for empty arguments.
	 * @param length The current length of the string. Used for recursion.
	 * @return A string representation of the match.
	 */
	private String buildMatchString(MatchNode match, ArrayList<int[]> argRanges,
			ArrayList<int[]> blankArgRanges, boolean showBlanks, int length) {

		if (match instanceof EffectMatchNode) {

			StringBuilder sb = new StringBuilder();
			String[] tokens = ((EffectMatchNode) match).getPattern().split("\\s+");
	
			if (length != 0 && showBlanks) sb.append("(");
			
			for (String token : tokens) {

				if (token.startsWith("$")) {
	
					MatchNode child = ((EffectMatchNode) match).getChild(token.substring(1));
					sb.append(buildMatchString(child, argRanges, blankArgRanges,
							showBlanks, length + sb.length()) + " ");
				}
				else sb.append(token + " ");
			}
			
			sb.deleteCharAt(sb.length() - 1);
			
			if (length != 0 && showBlanks) sb.append(")");
			
			return sb.toString();
		}
		else {
			
			ArgumentMatchNode argNode = (ArgumentMatchNode)match;
			
			String token = argNode.getArgument();
			
			if (token.isEmpty()) {

				if (showBlanks) token = "<" + argNode.getParameter().getName() + ">";

				blankArgRanges.add(new int[] {length, token.length()});
			}

			argRanges.add(new int[] {length, token.length()});
			return token;
		}
	}
	
	/**
	 * Clears the match list, and also the visual listings.
	 */
	private void clearMatches() {

		if (buttonBar != null && !buttonBar.isDisposed()) buttonBar.dispose();

		for (StyledText matchText : matchTexts) {
			
			matchText.getShell().setBackground(PlatformUI.getWorkbench().getDisplay()
					.getSystemColor(SWT.COLOR_WHITE));
			
			matchText.setBackground(PlatformUI.getWorkbench().getDisplay()
					.getSystemColor(SWT.COLOR_WHITE));
			
			matchText.getShell().setVisible(false);
		}

		matches.clear();
		selection = -1;
	}
	
	/**
	 * Select a match in the results listing by offset.
	 * @param offset The selection offset. For example, if the 4th result is selected, and the
	 * offset is 1, then the 5th result will be selected.
	 */
	private void selectMatchByOffset(int offset) {
		
		if (matches.isEmpty()) return;
		if (matches.size() == 1 && selection == 0) return;
		
		if (selection != -1) {
			
			// Close the refinement dialog if it's open.
			if (refineDialog != null && !refineDialog.isDisposed()) refineDialog.close();
			
			// Unhighlight the currently selected result.
			matchTexts.get(selection).setBackground(
					PlatformUI.getWorkbench().getDisplay()
					.getSystemColor(SWT.COLOR_WHITE));

			// Unhighlight the currently selected result.
			for (StyleRange styleRange : matchTexts.get(selection).getStyleRanges()) {
		
				matchTexts.get(selection).setStyleRange(
						new StyleRange(styleRange.start, styleRange.length,
								styleRange.foreground, matchTexts.get(selection)
								.getBackground(), styleRange.fontStyle));
			}
		}

		// Change the selection index.
		selection = (selection + matches.size() + offset) % matches.size();
		
		StyledText matchText = matchTexts.get(selection);
		if (matchText.isDisposed()) return;

		// Highlight the newly selected result.
		matchText.setBackground(matchSelectBackColor);

		// Highlight the newly selected result.
		for (StyleRange styleRange : matchTexts.get(selection).getStyleRanges()) {
	
			matchText.setStyleRange(
					new StyleRange(styleRange.start, styleRange.length,
							styleRange.foreground, matchTexts.get(selection)
							.getBackground(), styleRange.fontStyle));
		}
		
		try {
			
			// Preview the newly selected result, and move the button bar.

			env.reset();
			((JavaSnippetMatchEnvironment) env).applyMatch(matches.get(selection), false, getTotalHeight());
			
			if (buttonBar != null && !buttonBar.isDisposed()) buttonBar.close();
			buttonBar = createBar(false);
			
			adjustShellLocations();
		}
		catch (Exception e) {
			
			e.printStackTrace();
			env.reset();
			createBar(true);
		}
		
		queryText.setFocus();
	}
	
	/**
	 * Adjust the locations of the query box, result listings, and the button bar
	 * to account for the shift in the code from the previewed snippet.
	 */
	private void adjustShellLocations() {

		if (env instanceof JavaSnippetMatchEnvironment) {
			
			int oldY = shell.getLocation().y;

			shell.setLocation(shell.getLocation().x,
					((JavaSnippetMatchEnvironment) env).getSearchBoxAnchor(getTotalHeight()).y);
			
			for (StyledText mt : matchTexts) {

				if (!mt.isDisposed() && !mt.getShell().isDisposed()) {
					mt.getShell().setLocation(mt.getShell().getLocation().x,
							mt.getShell().getLocation().y + shell.getLocation().y - oldY);
				}
			}
			
			if (!buttonBar.isDisposed()) {
				buttonBar.setLocation(buttonBar.getLocation().x,
						buttonBar.getLocation().y + shell.getLocation().y - oldY);
			}
		}
	}
	
	/**
	 * Created the button bar to hold all the feedback buttons and warning icon.
	 * @param badSnippet If this is true, the button bar will simply say that the snippet has errors,
	 * instead of displaying the normal buttons.
	 * @return
	 */
	private Shell createBar(boolean badSnippet) {
		
		Shell bar = new Shell(shell, SWT.NO_TRIM | SWT.NO_FOCUS);
		bar.addFocusListener(ignoredListener);
		bar.setLayout(new FillLayout());
		
		Composite barComposite = new Composite(bar, SWT.BORDER | SWT.NO_FOCUS);
		barComposite.setBackground(PlatformUI.getWorkbench().getDisplay().
				getSystemColor(SWT.COLOR_WHITE));
		
		barComposite.addFocusListener(ignoredListener);
		
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.fill= true;
		layout.wrap = false;
		barComposite.setLayout(new RowLayout());
		
		if (badSnippet) {

			Label errorLabel = new Label(barComposite, SWT.NONE);
			errorLabel.setText("Snippet contains errors!");
			errorLabel.setBackground(PlatformUI.getWorkbench().getDisplay().
					getSystemColor(SWT.COLOR_WHITE));
			errorLabel.setForeground(PlatformUI.getWorkbench().getDisplay().
					getSystemColor(SWT.COLOR_RED));
		}
		else {
			
			if (env instanceof JavaSnippetMatchEnvironment) {

				String[] helpers = ((JavaSnippetMatchEnvironment) env).getNewHelpers();

				if (helpers.length != 0) {
					
					Label warningLabel = new Label(barComposite, SWT.NONE);
					warningLabel.setImage(SnipMatchPlugin.getDefault().getImageRegistry().get("warning"));
					
					String warningText = "Helper class(es) to be added:\n\n";
					
					for (String helper : helpers) {
						warningText += "- " + helper + ", ";
					}
					
					warningText = warningText.substring(0, warningText.length() - 2);
					warningLabel.setToolTipText(warningText);
				}
			}
			
			Label rateUpLabel = new Label(barComposite, SWT.NONE);
			rateUpLabel.setImage(SnipMatchPlugin.getDefault().getImageRegistry().get("thumbs_up"));
			rateUpLabel.setToolTipText("Rate Up");
			rateUpLabel.setCursor(handCursor);
			
			rateUpLabel.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseUp(MouseEvent e) {}
				
				@Override
				public void mouseDown(MouseEvent e) {
					
					ISendFeedbackListener listener = new ISendFeedbackListener() {

						@Override
						public void sendFeedbackSucceeded() {
							
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									
									showMessageBox("Result rated up!", false);
								}
							});
						}

						@Override
						public void sendFeedbackFailed(final String error) {

							
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								
								@Override
								public void run() {

									showMessageBox(error, true);
								}
							});
						}
					};

					if (!client.isWorking())
						client.startSendFeedback(queryText.getText(), matches.get(selection),
								null, 1, false, false, false, SnipMatchPlugin.getClientId(), false, listener);
				}
				
				@Override
				public void mouseDoubleClick(MouseEvent e) {}
			});
			
			Label rateDownLabel = new Label(barComposite, SWT.NONE);
			rateDownLabel.setImage(SnipMatchPlugin.getDefault().getImageRegistry().get("thumbs_down"));
			rateDownLabel.setToolTipText("Rate Down");
			rateDownLabel.setCursor(handCursor);
			
			rateDownLabel.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseUp(MouseEvent e) {}
				
				@Override
				public void mouseDown(MouseEvent e) {
					
					ISendFeedbackListener listener = new ISendFeedbackListener() {

						@Override
						public void sendFeedbackSucceeded() {
							
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								
								@Override
								public void run() {

									showMessageBox("Result rated down!", false);
								}
							});
						}

						@Override
						public void sendFeedbackFailed(final String error) {

							
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								
								@Override
								public void run() {

									showMessageBox(error, true);
								}
							});
						}
					};

					if (!client.isWorking())
						client.startSendFeedback(queryText.getText(), matches.get(selection),
								null, -1, false, false, false, SnipMatchPlugin.getClientId(), false, listener);
				}
				
				@Override
				public void mouseDoubleClick(MouseEvent e) {}
			});
			
			Label flagLabel = new Label(barComposite, SWT.NONE);
			flagLabel.setImage(SnipMatchPlugin.getDefault().getImageRegistry().get("flag"));
			flagLabel.setToolTipText("Flag");
			flagLabel.setCursor(handCursor);
			
			flagLabel.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseUp(MouseEvent e) {}
				
				@Override
				public void mouseDown(MouseEvent e) {
					
					ISendFeedbackListener listener = new ISendFeedbackListener() {

						@Override
						public void sendFeedbackSucceeded() {
							
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								
								@Override
								public void run() {

									showMessageBox("Result flagged!", false);
								}
							});
						}

						@Override
						public void sendFeedbackFailed(final String error) {

							
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								
								@Override
								public void run() {

									showMessageBox(error, true);
								}
							});
						}
					};

					if (!client.isWorking())
						client.startSendFeedback(queryText.getText(), matches.get(selection),
								null, 0, true, false, false, SnipMatchPlugin.getClientId(), false, listener);
				}
				
				@Override
				public void mouseDoubleClick(MouseEvent e) {}
			});
			
			Label commentLabel = new Label(barComposite, SWT.NONE);
			commentLabel.setImage(SnipMatchPlugin.getDefault().getImageRegistry().get("comment"));
			commentLabel.setToolTipText("Comment");
			commentLabel.setCursor(handCursor);
			
			commentLabel.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseUp(MouseEvent e) {}
				
				@Override
				public void mouseDown(MouseEvent e) {
					
					showCommentDialog();
				}
				
				@Override
				public void mouseDoubleClick(MouseEvent e) {}
			});
		}
		
		bar.pack();

		Shell matchShell = matchTexts.get(selection).getShell();
		bar.setLocation(matchShell.getLocation().x + matchShell.getSize().x - 1,
				matchShell.getLocation().y + matchShell.getSize().y - bar.getSize().y);

		bar.open();

		return bar;
	}
	
	/**
	 * Shows a message box as part of the search GUI.
	 * This way, we can use the custom FocusListener to prevent the search GUI from closing when
	 * the message box is displayed.
	 * @param message The message to display.
	 * @param error Whether or not this message is an error.
	 */
	private void showMessageBox(String message, boolean error) {
		
		final Shell msgBox = new Shell(buttonBar, SWT.APPLICATION_MODAL);
		msgBox.setBackground(PlatformUI.getWorkbench().getDisplay()
				.getSystemColor(SWT.COLOR_WHITE));
		msgBox.setText("SnipMatch");
		msgBox.setLocation(buttonBar.getLocation().x, buttonBar.getLocation().y - 3);

		if (error) msgBox.setForeground(PlatformUI.getWorkbench().getDisplay()
				.getSystemColor(SWT.COLOR_RED));
		
		msgBox.addFocusListener(ignoredListener);
		
		RowLayout rl = new RowLayout();
		msgBox.setLayout(rl);
		
		Label msgLabel;
		Button okButton;
		
		msgLabel = new Label(msgBox, SWT.NONE);
		msgLabel.setBackground(PlatformUI.getWorkbench().getDisplay()
				.getSystemColor(SWT.COLOR_WHITE));
		msgLabel.setText("  " + message + "  ");
		
		if (error) msgLabel.setForeground(PlatformUI.getWorkbench().getDisplay()
				.getSystemColor(SWT.COLOR_RED));
		
		okButton = new Button(msgBox, SWT.PUSH);
		okButton.setText("OK");

		okButton.addFocusListener(ignoredListener);
		
		okButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				msgBox.close();
				queryText.setFocus();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				msgBox.close();
				queryText.setFocus();
			}
		});

		msgBox.setDefaultButton(okButton);
		msgBox.pack();
		msgLabel.setLocation(msgLabel.getLocation().x, 7);
		msgBox.open();
	}
	
	/**
	 * Shows a dialog that allows the user to submit a comment about a snippet.
	 */
	private void showCommentDialog() {
		
		final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.CLOSE);
		dialog.setText("User Comment");
		dialog.setSize(260, 235);
		dialog.addFocusListener(ignoredListener);
		
		FormLayout fl = new FormLayout();
		fl.marginWidth = 5;
		fl.marginHeight = 5;
		fl.spacing = 5;
		dialog.setLayout(fl);

		FormData fd;
		Text dialogQueryText;
		Text matchText;
		final Text commentText;
		Button submitButton;
		Button cancelButton;
		
		{
			Label queryLabel = new Label(dialog, SWT.NONE);
			queryLabel.setText("Query:");

			fd = new FormData();
			fd.left = new FormAttachment(0, 50);
			fd.right = new FormAttachment(100);

			dialogQueryText = new Text(dialog, SWT.BORDER);
			dialogQueryText.setText(queryText.getText());
			dialogQueryText.setEditable(false);
			dialogQueryText.setLayoutData(fd);
			dialogQueryText.addFocusListener(ignoredListener);
		}
		
		{
			fd = new FormData();
			fd.top = new FormAttachment(dialogQueryText);

			Label matchLabel = new Label(dialog, SWT.NONE);
			matchLabel.setText("Result:");
			matchLabel.setLayoutData(fd);
			
			fd = new FormData();
			fd.left = new FormAttachment(dialogQueryText, 0, SWT.LEFT);
			fd.right = new FormAttachment(100);
			fd.top = new FormAttachment(dialogQueryText);
			
			matchText = new Text(dialog, SWT.BORDER);
			matchText.setText(matchTexts.get(selection).getText());
			matchText.setEditable(false);
			matchText.setLayoutData(fd);
			matchText.addFocusListener(ignoredListener);
		}
		
		{
			fd = new FormData();
			fd.top = new FormAttachment(matchText);
			
			Label commentLabel = new Label(dialog, SWT.NONE);
			commentLabel.setText("Comment:");
			commentLabel.setLayoutData(fd);
			
			fd = new FormData();
			fd.left = new FormAttachment(0);
			fd.right = new FormAttachment(100);
			fd.top = new FormAttachment(commentLabel);
			fd.bottom = new FormAttachment(commentLabel, 100, SWT.BOTTOM);
			
			commentText = new Text(dialog,
					SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
			commentText.setLayoutData(fd);
			commentText.addFocusListener(ignoredListener);
			commentText.setFocus();
		}
		
		{
			fd = new FormData();
			fd.left = new FormAttachment(100, -100);
			fd.right = new FormAttachment(100);
			fd.bottom = new FormAttachment(100);
	
			cancelButton = new Button(dialog, SWT.NONE);
			cancelButton.setText("Cancel");
			cancelButton.setLayoutData(fd);
			cancelButton.addFocusListener(ignoredListener);
			
			cancelButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					dialog.close();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					dialog.close();
				}
			});
		}
		
		{
			fd = new FormData();
			fd.left = new FormAttachment(cancelButton, -100, SWT.LEFT);
			fd.right = new FormAttachment(cancelButton);
			fd.bottom = new FormAttachment(100);
	
			submitButton = new Button(dialog, SWT.NONE);
			submitButton.setText("Submit");
			submitButton.setLayoutData(fd);
			submitButton.addFocusListener(ignoredListener);
			
			submitButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					submitComment();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					submitComment();
				}
				
				private void submitComment() {
					
					ISendFeedbackListener listener = new ISendFeedbackListener() {

						@Override
						public void sendFeedbackSucceeded() {
							
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								
								@Override
								public void run() {

									showMessageBox("Comment submitted!", false);
								}
							});
						}

						@Override
						public void sendFeedbackFailed(final String error) {

							
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								
								@Override
								public void run() {

									showMessageBox(error, true);
								}
							});
						}
					};

					if (!client.isWorking())
						client.startSendFeedback(queryText.getText(), matches.get(selection),
								commentText.getText(), 0, false, false, false, SnipMatchPlugin.getClientId(), false, listener);
						
					dialog.close();
				}
			});
			
			dialog.setDefaultButton(submitButton);
		}
		
		dialog.open();
	}
	
	/**
	 * Shows the refinement dialog.
	 */
	private void showRefinementDialog() {
		
		selectionConfirmed = false;
		
		refineDialog = new Shell(shell, SWT.NO_BACKGROUND | SWT.NO_TRIM);
		refineDialog.setText("Snippet Refinement");
		refineDialog.setSize(280, 280);
		refineDialog.addFocusListener(ignoredListener);
		refineDialog.setLayout(new FillLayout());

		Shell matchShell = matchTexts.get(selection).getShell();
		refineDialog.setLocation(matchShell.getLocation().x + matchShell.getSize().x + 3,
				matchShell.getLocation().y);
		
		Composite mainArea = new Composite(refineDialog, SWT.BORDER);
		mainArea.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		RowLayout rl = new RowLayout(SWT.VERTICAL);
		rl.marginWidth = 5;
		rl.marginHeight = 5;
		rl.spacing = 5;
		mainArea.setLayout(rl);
		
		// Make a working copy of the selected match, so we can refine it.
		refinedMatch = (EffectMatchNode) matches.get(selection).clone();

		boolean highlighted = createRefinementControls(refinedMatch, mainArea, false);
		
		// Create the buttons.

		Button applyButton;
		Button cancelButton;
		
		Composite buttonArea = new Composite(mainArea, SWT.NONE);
		buttonArea.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		rl = new RowLayout(SWT.HORIZONTAL);
		rl.spacing = 5;
		buttonArea.setLayout(rl);
		
		{
			applyButton = new Button(buttonArea, SWT.NONE);
			applyButton.setText("Apply");
			applyButton.addFocusListener(ignoredListener);
			
			applyButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					
					applyRefinement();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {

					applyRefinement();
				}
				
				private void applyRefinement() {

					selectionConfirmed = true;
					matches.set(selection, refinedMatch);
					refineDialog.close();
					shell.close();
				}
			});
			
			refineDialog.setDefaultButton(applyButton);
			// If a refinement field was not focused by default, then focus the apply button.
			if (!highlighted) applyButton.setFocus();
		}
		
		{
			cancelButton = new Button(buttonArea, SWT.NONE);
			cancelButton.setText("Cancel");
			cancelButton.addFocusListener(ignoredListener);
			
			cancelButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					refineDialog.close();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					refineDialog.close();
				}
			});
		}
		
		refineDialog.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent evt) {

				try {
					client.cancelWork();
					env.reset();
					((JavaSnippetMatchEnvironment) env).applyMatch(matches.get(selection), false, getTotalHeight());
					adjustShellLocations();
					shell.setFocus();
				}
				catch (Exception e) {
					e.printStackTrace();
					env.reset();
				}
				
				refineDialog = null;
			}
		});

		refineDialog.pack();
		refineDialog.open();
	}
	
	/**
	 * Recursively create refinement controls. This is for handling nested results, which are not currently used.
	 * @param match The match node being refined.
	 * @param parent The parent control.
	 * @param highlighted Whether or not an earlier refinement field was already focused.
	 * @return Whether or not one of the refinement fields created was focused.
	 * By default, the first empty field is focused.
	 */
	private boolean createRefinementControls(MatchNode match, Composite parent, boolean highlighted) {
		
		if (match instanceof EffectMatchNode) {
			
			EffectMatchNode effectMatch = (EffectMatchNode) match;
			
			Group refinementGroup = new Group(parent, SWT.NONE);
			refinementGroup.setBackground(PlatformUI.getWorkbench().
					getDisplay().getSystemColor(SWT.COLOR_WHITE));
			
			// Show the prettified search pattern for the current match node.
			
			String prettyPattern = effectMatch.getPattern();
			
			while (prettyPattern.contains("$")) {
				
				int dollar = prettyPattern.indexOf("$");
				int nextSpace = prettyPattern.indexOf(" ", dollar);
				if (nextSpace == -1) nextSpace = prettyPattern.length();

				prettyPattern = prettyPattern.substring(0, dollar) + "<" +
				prettyPattern.substring(dollar + 1, nextSpace) + ">" +
				prettyPattern.substring(nextSpace);
			}
			
			refinementGroup.setText(prettyPattern);
			
			RowLayout rl = new RowLayout(SWT.VERTICAL);
			rl.marginWidth = 5;
			rl.marginHeight = 5;
			rl.spacing = 5;
			refinementGroup.setLayout(rl);
			
			// Create controls for all the child nodes.
			for (int i = 0; i < effectMatch.numChildren(); i++) {
				highlighted = createRefinementControls(effectMatch.getChild(i),
						refinementGroup, highlighted);
			}
		}
		else {

			final ArgumentMatchNode argMatch = (ArgumentMatchNode) match;
			
			// Create a text field to edit the argument.
			
			Composite argEntry = new Composite(parent, SWT.NONE);
			argEntry.setBackground(PlatformUI.getWorkbench().
					getDisplay().getSystemColor(SWT.COLOR_WHITE));
			
			Label paramLabel = new Label(argEntry, SWT.NONE);
			paramLabel.setBackground(PlatformUI.getWorkbench().
					getDisplay().getSystemColor(SWT.COLOR_WHITE));
			paramLabel.setSize(80, 20);
			paramLabel.setText(argMatch.getParameter().getName() + ":");
			paramLabel.setToolTipText(argMatch.getParameter().getMinorType());
			
			final Text argText = new Text(argEntry, SWT.BORDER);
			argText.setLocation(80, 0);
			argText.setSize(200, 20);
			argText.setText(argMatch.getArgument());
			argText.setToolTipText(argMatch.getParameter().getFullType());

			argText.addFocusListener(ignoredListener);
			
			// Uncomment for proposals in the refinement dialog.
			/*
			final SimpleContentProposalProvider scp = new SimpleContentProposalProvider(new String[0]);
	
			ContentProposalAdapter adapter = new ContentProposalAdapter(
					argText, new TextContentAdapter(), scp, null, null);
   
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			adapter.setPropagateKeys(true);
			adapter.setPopupSize(new Point(argText.getSize().x, 110));
			*/
			
			argText.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent evt) {
					
					String arg = argText.getText();
					
					if (arg.endsWith("\t")) {
						argText.setText(arg.substring(0, arg.length() - 1));
						return;
					}
					
					argMatch.setArgument(argText.getText());

					// Uncomment for proposals in the refinement dialog.
					/*
					ArrayList<String> proposals = new ArrayList<String>();
					
					for (String proposal : env.getArgumentCompletions(argMatch)) {
						proposals.add(proposal);
					}
					
					proposals.remove("");
					
					if (!proposals.contains(argText.getText()))
						proposals.add(argText.getText());
					
					scp.setProposals(proposals.toArray(new String[proposals.size()]));
					*/
					
					try {
						// Dynamically update the preview when any refinement field is modified.
						env.reset();
						((JavaSnippetMatchEnvironment) env).applyMatch(refinedMatch, false, getTotalHeight());
					}
					catch (Exception e) {
						e.printStackTrace();
					}

					adjustShellLocations();
				}
			});

			// Highlight the first empty argument.
			if (!highlighted && argText.getCharCount() == 0) {
				argText.setFocus();
				highlighted = true;
			}
		}
		
		return highlighted;
	}
	
	public void hide() {
		
		if (!shell.isDisposed()) shell.close();
	}

	/**
	 * Lock the search box while logging in, and display a message.
	 */
	public void lock() {
		
		if (queryText != null) {
			
			queryText.setEditable(false);
			queryText.setText("Logging in...");
			queryText.setBackground(queryDisabledBackColor);
		}
	}

	/**
	 * Release the lock on the search box once logged in.
	 */
	public void release() {

		if (queryText != null) {
			
			queryText.setText("");
			queryText.setEditable(true);
			queryText.setBackground(queryBackColor);
		}
	}
}
