/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.rcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.IndentUtil;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.recommenders.snipmatch.core.ArgumentMatchNode;
import org.eclipse.recommenders.snipmatch.core.Effect;
import org.eclipse.recommenders.snipmatch.core.EffectMatchNode;
import org.eclipse.recommenders.snipmatch.core.EffectParameter;
import org.eclipse.recommenders.snipmatch.core.FormulaSnippetNode;
import org.eclipse.recommenders.snipmatch.core.MatchNode;
import org.eclipse.recommenders.snipmatch.core.SnippetMatchEnvironment;
import org.eclipse.recommenders.snipmatch.core.TextSnippetNode;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.PlatformUI;

/**
 * This match environment is specifically for integrating Java code snippets in the Eclipse editor.
 */
@SuppressWarnings("restriction")
public class JavaSnippetMatchEnvironment extends SnippetMatchEnvironment {
	
	/**
	 * Sorts arguments based on length.
	 */
	private class ArgumentSorter implements Comparator<String> {
		
		public int compare(String a1, String a2) {
			
			return (int)Math.signum(a1.length() - a2.length());
		}
	}
	
	// Temporary markers used in code integration.
	private static final String CURSOR_MARKER = "/*" + "${cursor}" + "*/";
	private static final String END_MARKER = "/*" + "${end}" + "*/";
	
	// Maximum number of completions for each argument.
	private static final int MAX_ARG_COMPLETIONS = 5;

	// text operation objects
	private JavaEditor editor;
	private StyledText styledText;
	private ITextOperationTarget textOp;
	private int selOffset;
	private int selLength;
	private IDocument doc;	
	private int lastLine;

	// Java elements
	private IJavaProject project;
	private ICompilationUnit unit;
	
	// environment status members
	private String originalText;
	private ArrayList<String> newImports;
	private ArrayList<String[]> newHelpers;
	private boolean readingHelper;
	private boolean clean;
	private boolean confirmed;
	private int searchBoxHeight;
	
	// type checking objects
	private Hashtable<String, ArrayList<String>> compatibles;
	
	// code assist cache
	private Hashtable<String, ArrayList<CompletionProposal>> codeAssistCache;
	
	public JavaSnippetMatchEnvironment() {

		// Get the active text editor.
		editor = (JavaEditor) PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		
		if (editor != null) {

			// Get the necessary objects for text operations, and save a copy of the unmodified code.
			doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			textOp = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
			originalText = doc.get();
		
			// get current code selection
			ITextSelection selection = (ITextSelection)
			editor.getSelectionProvider().getSelection();
			selOffset = selection.getOffset();
			selLength = selection.getLength();

			IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);

			if (file != null) {

				// Get access to the Java elements of the currently open file.
				unit = (ICompilationUnit) JavaCore.create(file);
				project = JavaCore.create(file.getProject());
			}
		}
		
		// Create a table of compatible primitive types. There should be a simpler way to check primitive type compatibility.
		
		compatibles = new Hashtable<String, ArrayList<String>>();

		compatibles.put("float", new ArrayList<String>());
		compatibles.get("float").add("int");
		compatibles.get("float").add("long");
		compatibles.get("float").add("short");
		compatibles.get("float").add("byte");

		compatibles.put("double", new ArrayList<String>());
		compatibles.get("double").add("int");
		compatibles.get("double").add("long");
		compatibles.get("double").add("short");
		compatibles.get("double").add("byte");
		compatibles.get("double").add("float");

		compatibles.put("long", new ArrayList<String>());
		compatibles.get("long").add("int");
		compatibles.get("long").add("short");
		compatibles.get("long").add("byte");

		compatibles.put("int", new ArrayList<String>());
		compatibles.get("int").add("char");
		compatibles.get("int").add("short");
		compatibles.get("int").add("byte");

		compatibles.put("short", new ArrayList<String>());
		compatibles.get("short").add("byte");
		compatibles.get("short").add("char");

		compatibles.put("byte", new ArrayList<String>());
		compatibles.get("byte").add("char");

		compatibles.put("char", new ArrayList<String>());
		compatibles.get("char").add("int");
		compatibles.get("char").add("short");
		compatibles.get("char").add("byte");
		
		compatibles.put("$number", new ArrayList<String>());
		compatibles.get("$number").add("int");
		compatibles.get("$number").add("long");
		compatibles.get("$number").add("short");
		compatibles.get("$number").add("byte");
		compatibles.get("$number").add("char");
		compatibles.get("$number").add("float");
		compatibles.get("$number").add("double");

		// We will be using Code Assist to help with code integration, so we cache the Code Assist results.
		codeAssistCache = new Hashtable<String, ArrayList<CompletionProposal>>();

		newImports = new ArrayList<String>();
		newHelpers = new ArrayList<String[]>();
		readingHelper = false;
		clean = true;

		// The styled text helps us obtain actual visual coordinates.
		styledText = (StyledText) editor.getAdapter(Control.class);
	}

	@Override
	public String getName() {

		return "javasnippet";
	}

	@Override
	public String getFriendlyName() {
		
		return "Java Snippet";
	}

	@Override
	public String[] getMajorTypes() {

		return new String[] {"expr", "stmt", "ident"};
	}
	
	public String[] getFriendlyMajorTypes() {

		return new String[] {"Expression", "Statement", "Identifier"};
	}
	
	/**
	 * Returns an array of new imports added to the environment.
	 * @return An array of new imports added to the environment.
	 */
	public String[] getNewImports() {
		
		return newImports.toArray(new String[newImports.size()]);
	}
	
	/**
	 * Returns an array of info strings about all the helper classes added to the environment.
	 * @return An array of info strings about all the helper classes added to the environment.
	 */
	public String[] getNewHelpers() {
		
		String[] helperInfo = new String[newHelpers.size()];
		
		// Each element in helperInfo looks something like this: "MyHelperClass: 65 line(s)"
		
		for (int i = 0; i < helperInfo.length; i++) {
			helperInfo[i] = newHelpers.get(i)[0] + ": " +
			newHelpers.get(i)[1].split("\\r?\\n").length + " line(s)";
		}
		
		return helperInfo;
	}
	
	public IJavaProject getProject() {
		
		return project;
	}

	@Override
	public boolean testMatch(MatchNode match) {
		
		/* This method tests nested matches for type incompatibilities. Since nested results
		 * are not currently returned, this test will always pass.
		 */

		if (match instanceof EffectMatchNode) {

			EffectMatchNode effectMatch = (EffectMatchNode) match;
			Effect effect = effectMatch.getEffect();
			
			for (int i = 0; i < effectMatch.numChildren(); i++) {
				
				if (!testMatch(effectMatch.getChild(i))) return false;
				
				EffectParameter param = effect.getParameter(i);
				
				if (effectMatch.getChild(i) instanceof EffectMatchNode) {
					
					EffectMatchNode child = (EffectMatchNode) effectMatch.getChild(i);
					Effect childEffect = child.getEffect();

					if (param.getMajorType().equals("expr")) {
						
						if (param.getMinorType() != null && !param.getMinorType().isEmpty()) {

							if (childEffect.getMinorType() == null ||
									childEffect.getMinorType().isEmpty()) return false;
							
							if (childEffect.getMinorType().equals(param.getMinorType())) return true;
							
							if (param.getMinorType().equals("$array") ||
									param.getMinorType().equals("[]")) {
								
								if (childEffect.getMinorType().endsWith("[]")) return true;
							}
							
							if (param.getMinorType().equals("$loopable")) {
								
								if (childEffect.getMinorType().endsWith("[]")) return true;

								try {
									
									Class<?> parentClass = Iterable.class;
									Class<?> childClass = Class.forName(childEffect.getMinorType(),
											false, null);
									
									return parentClass.isAssignableFrom(childClass);
								} 
								catch (ClassNotFoundException e) {}
							}
							
							for (Entry<String, ArrayList<String>> entry : compatibles.entrySet()) {
								
								if (entry.getKey().equals(param.getMinorType()) &&
										entry.getValue().contains(childEffect.getMinorType())) {
									return true;
								}
							}

							try {
								
								Class<?> parentClass = Class.forName(param.getMinorType(),
										false, null);
								Class<?> childClass = Class.forName(childEffect.getMinorType(),
										false, null);
								
								return parentClass.isAssignableFrom(childClass);
							}
							catch (ClassNotFoundException e) {}
							
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Returns an appropriate anchor point for the search box based on the current caret location.
	 * @param searchBoxHeight The height of the search box.
	 * @return An appropriate anchor point for the search box based on the current caret location.
	 */
	public Point getSearchBoxAnchor(int searchBoxHeight) {
		if (clean) {
			// Initially, return the point right below the caret.
			return styledText.toDisplay(styledText.getCaret().getLocation().x,
					styledText.getCaret().getLocation().y + styledText.getCaret().getSize().y);
		}
		else {
			// For all subsequent queries, simply return a vertical offset based on the end of the inserted snippet.
			int endOfSnippet = styledText.getLinePixel(lastLine + 1) + 2;
			return styledText.toDisplay(0, endOfSnippet);
		}
	}

	@Override
	public void reset() {
		
		if (clean) return;
	
		// Undo the code changes.
		textOp.doOperation(ITextOperationTarget.UNDO);
		
		// Restore the original code selection.
		editor.selectAndReveal(selOffset, selLength);
		
		try {
			unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		// Restore status objects.

		newHelpers.clear();
		newImports.clear();

		clean = true;
	}
	
	@Override
	public String[] getArgumentCompletions(final ArgumentMatchNode argNode) {

		ArrayList<String> completions = new ArrayList<String>();
		String arg = argNode.getArgument();
		boolean exactMatch = false;
		String minorType = null;

		// Do not complete empty arguments.
		if (arg.isEmpty()) return new String[] { "" };
	
		try {
			
			// If we have not already, search the code for an exact name match for our argument, regardless of type.
			
			String argTest = "/**//**/";

			if (!codeAssistCache.containsKey(argTest)) {

				ICompilationUnit cloneUnit = unit.getWorkingCopy(null);
				int testOffset = selOffset + argTest.length() - 4;
				cloneUnit.getBuffer().setContents(originalText);
		
				final ArrayList<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

				cloneUnit.applyTextEdit(new ReplaceEdit(
						selOffset, selLength, argTest), null);
	
				cloneUnit.codeComplete(testOffset, new CompletionRequestor() {
					
					@Override
					public void accept(CompletionProposal proposal) {
							
						proposals.add(proposal);
					}
				});
				
				codeAssistCache.put(argTest, proposals);
				
				cloneUnit.discardWorkingCopy();
			}
			
			// Process completion proposals, and isolate the exact name matches.
			for (CompletionProposal proposal : codeAssistCache.get(argTest)) {

				String completion = new String(proposal.getCompletion());
				if (completion.equals(arg)) exactMatch = true;
			}
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		// If the argument is an expression...
		if (argNode.getParameter().getMajorType().equals("expr")) {
	
			try {
				
				/* If we have not already, perform a type-sensitive code completion on the argument.
				 * This works by temporarily inserting a dummy variable declaration into the code,
				 * and using code assist to see which values can be used to initialize it.
				 */
				
				ICompilationUnit cloneUnit = unit.getWorkingCopy(null);
				cloneUnit.getBuffer().setContents(originalText);
				
				final String dummyName = "__omgwtfbbqlol__";
				String argTest = null;
				int minRelevance = 0;
					
				minorType = argNode.getParameter().getMinorType();
	
				if (!minorType.isEmpty() && !minorType.equals("$number") &&
					!minorType.equals("$array") && !minorType.equals("$loopable") &&
					!minorType.equals("Object")) {
					
					argTest = "/**/" + minorType + " " + dummyName + " = " + arg + "/**/";
					minRelevance = 32;
				}
				else argTest = "/**/" + arg + "/**/";
	
				int testOffset = selOffset + argTest.length() - 4;
				
				cloneUnit.applyTextEdit(new ReplaceEdit(selOffset, selLength, argTest), null);
				String contents = cloneUnit.getBuffer().getContents();
				
				if (!codeAssistCache.containsKey(argTest)) {
					
					final ArrayList<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
		
					cloneUnit.codeComplete(testOffset, new CompletionRequestor() {
						
						@Override
						public void accept(CompletionProposal proposal) {
							
							proposals.add(proposal);
						}
					});
					
					codeAssistCache.put(argTest, proposals);
				}
				
				cloneUnit.discardWorkingCopy();
				
				// Filter the results based on type and relevance.
				for (CompletionProposal proposal : codeAssistCache.get(argTest)) {

					if (proposal.getRelevance() < minRelevance) continue;

					String firstPart = contents.substring(testOffset - arg.length(), proposal.getReplaceStart());
					String secondPart = new String(proposal.getCompletion());
					String completion = firstPart + secondPart;
					
					if (completion.equals(dummyName)) continue;
					
					String signature = null;
					if (proposal.getSignature() != null)
						signature = new String(proposal.getSignature());

					switch (proposal.getKind()) {
					
					case CompletionProposal.FIELD_REF:
					case CompletionProposal.LOCAL_VARIABLE_REF:

						if (minorType.equals("$number")) {
							if (!isNumericType(signature)) break;
						}
						else if (minorType.equals("$array")) {
							if (!isArrayType(signature)) break;
						}
						else if (minorType.equals("$loopable")) {
							if (!isLoopableType(signature)) break;
						}

						completions.add(completion);
						
						break;
						
					case CompletionProposal.METHOD_REF:

						if (!argNode.getArgument().isEmpty() && signature.contains("()")) {
							
							String returnType = signature.substring(signature.lastIndexOf(")") + 1);
							
							if (minorType.equals("$number")) {
								if (!isNumericType(returnType)) break;
							}
							else if (minorType.equals("$array")) {
								if (!isArrayType(returnType)) break;
							}
							else if (minorType.equals("$loopable")) {
								if (!isLoopableType(returnType)) break;
							}

							completions.add(completion);
						}
						
						break;
					}
				}
			}
			catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		
		// Limit the number of completions for each argument.
		while (completions.size() > MAX_ARG_COMPLETIONS) {
			completions.remove(completions.size() - 1);
		}

		// If an exact match has not been found, try interpreting the argument as a literal or a new identifier.
		if (!exactMatch && !arg.endsWith(".")) {

			if (argNode.getParameter().getMajorType().equals("expr")) {
				
				if (minorType.equals("$number") || minorType.equals("int") ||
					minorType.equals("long") || minorType.equals("short") ||
					minorType.equals("byte") || minorType.equals("char") ||
					minorType.equals("float") || minorType.equals("double")) {

					if (arg.matches("[0-9]*")) completions.add(arg);
				}
				else if (minorType.equals("String")) {

					if (arg.startsWith("\"")) completions.add(arg);
				}
				else if (minorType.isEmpty()) {

					if (arg.matches("[0-9]*") || arg.startsWith("\"")) completions.add(arg);
				}
				else if (minorType.equals("$ident")) {

					if (!arg.matches("[0-9]*")) completions.add(arg);
				}
			}
		}
		
		// Sort the argument completions by length.
		Collections.sort(completions, new ArgumentSorter());

		/* Make sure exactly one of the completions for each argument is blank. A blank argument
		 * means that a grayed out parameter name will be shown instead. */
		completions.remove("");
		completions.add("");

		String[] result = completions.toArray(new String[completions.size()]);
		return result;
	}
	
	/**
	 * Determines if a type signature is that of a numeric type.
	 * @param typeName
	 * @return
	 */
	private boolean isNumericType(String typeName) {

		return typeName.equals("B") || typeName.equals("C") ||
			typeName.equals("S") || typeName.equals("I") ||
			typeName.equals("J") || typeName.equals("F") ||
			typeName.equals("D");
	}

	/**
	 * Determines if a type signature is that of an array type.
	 * @param typeName
	 * @return
	 */
	private boolean isArrayType(String typeName) {

		return typeName.startsWith("[");
	}

	/**
	 * Determines if a type signature is that of a "loopable" type.
	 * @param typeName
	 * @return
	 */
	private boolean isLoopableType(String typeName) {

		// Check to see if the type signature is of a "loopable" type.
		if (isArrayType(typeName)) return true;
		
		// Otherwise, check to see if it is a parameterized collection type.
		try {
			
			if (typeName.length() <= 1) return false;
			
			typeName = typeName.substring(1, typeName.length() - 1);
			
			int angleStart = typeName.indexOf('<');
			if (angleStart != -1) typeName = typeName.substring(0, angleStart);
			
			Class<?> parentClass = Iterable.class;
			Class<?> childClass = Class.forName(typeName, false, null);
			return parentClass.isAssignableFrom(childClass);
		} 
		catch (ClassNotFoundException e) {}
		
		return false;
	}

	protected String getQueryTokenInterpretation(String token) {

		if (token.startsWith("\"")) return "expr:String";
		else if (token.matches("[0-9]*")) return "expr:int";

		return "expr:" + getVariableType(token);
	}
	
	/**
	 * Special version of applyMatch, which takes in a few more arguments specific to this environment.
	 * @see org.eclipse.recommenders.snipmatch.core.MatchEnvironment#applyMatch(MatchNode)
	 * @param match The match to apply.
	 * @param confirmed False if this is just a code integration preview. True if this is a final integration.
	 * @param searchBoxHeight The height of the search box.
	 * @throws Exception
	 */
	public void applyMatch(MatchNode match, boolean confirmed, int searchBoxHeight) throws Exception {
		
		this.confirmed = confirmed;
		this.searchBoxHeight = searchBoxHeight;
		applyMatch(match);
	}

	/**
	 * Build MatchNode overview text for search result display module
	 * 
	 * @param node
	 * @return
	 * 
	 */
	public Object evaluateMatchNodeOverview(EffectMatchNode node) {
		String overViewText = (String) evaluateMatchNode(node);
		//Helper content, usually classes, ignore imports here
		String helper = null;
		int start = node.getEffect().getCode().indexOf("${helper}");
		int end = node.getEffect().getCode().indexOf("${endHelper}");
		if(start>-1 && end>-1 && end>start+9){
			helper = node.getEffect().getCode().substring(start+9, end);
		}
		
		//Remove cursor label from overview text
		if(overViewText !=null )
			overViewText = overViewText.replace("/*${cursor}*/", "");
		
		//Add helper classes tips
		if(helper != null && !helper.isEmpty())
			overViewText = overViewText +"\r\n Helper Classes:\r\n"+ helper;
		newImports.clear();
		newHelpers.clear();
		codeAssistCache.clear();
		return overViewText;
	}
	
	@Override
	protected void applyResult(Object result, Effect effect) throws Exception {

		clean = false;
		
		// This single, final text edit will have the combined effect of all code changes + imports + helper classes.
		MultiTextEdit finalEdit = new MultiTextEdit();
		
		// Remove whitespace from around the evaluated snippet, and append the temporary END marker.
		String snippet = ((String) result).trim() + END_MARKER;
		
		// helper names ===============================================
		
		for (int i = 0; i < newHelpers.size(); i++) {
			
			String[] helperInfo = newHelpers.get(i);
			String helperCode = helperInfo[1];
			String[] tokens = helperCode.split("\\s");
			
			for (int j = 0; j < tokens.length; j++) {

				// If the current token is "class", then the next token must be the desired helper class name.
				if (tokens[j].equals("class")) {

					// Acquire a free helper class name based on the requested helper name and helper ID.
					helperInfo[0] = getFreeHelperName(tokens[j + 1], helperInfo[2], unit);
					
					if (helperInfo[0] != null) {
						// Replace all instances of the requested helper class name in the helper's code with the free class name.
						snippet = snippet.replaceAll(tokens[j + 1], helperInfo[0]);
						helperInfo[1] = helperInfo[1].replaceAll(tokens[j + 1], helperInfo[0]);
					}
					
					break;
				}
			}
		}
		
		// snippet indentation ========================================

		// Insert the snippet into a working copy of the document.
		IDocument workingDoc = new Document(doc.get());
		workingDoc.replace(selOffset, selLength, snippet);
		
		int srcStartLine = workingDoc.getLineOfOffset(selOffset);
		int srcEndLine = workingDoc.getLineOfOffset(selOffset + snippet.length());
		
		// Apply indentation to the lines of the inserted snippet.
		IndentUtil.indentLines(workingDoc, new LineRange(srcStartLine,
				srcEndLine - srcStartLine + 1), project, null);
		
		lastLine = srcEndLine;  // Keep track of the last line.
		
		// Now, we extract the affected lines, and create a text edit out of it, so we can add it to the final edit.
		
		int srcStart = workingDoc.getLineOffset(srcStartLine);
		int srcEnd = workingDoc.getLineOffset(srcEndLine) +
		workingDoc.getLineLength(srcEndLine);
		
		int destStartLine = doc.getLineOfOffset(selOffset);
		int destEndLine = doc.getLineOfOffset(selOffset + selLength);
		
		int destStart = doc.getLineOffset(destStartLine);
		int destEnd = doc.getLineOffset(destEndLine) +
		doc.getLineLength(destEndLine);
		
		String formattedCode = workingDoc.get(srcStart, srcEnd - srcStart);
	
		// This version of the text edit still contains the special markers, so we can keep working with them.
		ReplaceEdit markerCodeEdit = new ReplaceEdit(destStart, destEnd - destStart,
				formattedCode);
		
		// This version of the text edit has the special markers removed, and is the one added to the final edit.
		ReplaceEdit codeEdit = new ReplaceEdit(destStart, destEnd - destStart,
				formattedCode.replace(CURSOR_MARKER, "").replace(END_MARKER, ""));
		
		workingDoc = new Document(doc.get());
		markerCodeEdit.copy().apply(workingDoc);
		
		finalEdit.addChild(codeEdit);
		
		// dummy package ===========================================
		
		/* The spacing of the document after editing Java elements varies based on whether or not a
		 * package statement exists, so for consistency, we add a temporary dummy package statement if there isn't one.
		 */

		ICompilationUnit cloneUnit = unit.getWorkingCopy(null);
		
		IPackageDeclaration dummyPackage = null;
		
		if (cloneUnit.getPackageDeclarations().length == 0) {
			dummyPackage = cloneUnit.createPackageDeclaration("___OMGWTFBBQPACKAGE___", null);
		}
		
		// helper insertion ========================================

		// We only insert helper classes if this is not just a preview.
		if (confirmed) {
			
			for (String[] helper : newHelpers) {

				if (helper[0] == null) continue;

				// Auto-generate an ID comment to the helper class source, and make sure the class is not private or public.
				String source = "// Snippet ID: " + effect.getId() + helper[1].
				replaceAll("public class", "class").
				replaceAll("private class", "class");

				// We create a temporary document just to indent the helper class code properly.
				
				IDocument helperIndentDoc = new Document(source);
				
				IndentUtil.indentLines(helperIndentDoc,
						new LineRange(0, helperIndentDoc.getNumberOfLines()),
						project, null);
				
				source = helperIndentDoc.get();  // Get the indented helper class source code.
				
				// Try to create the helper class in the working copy.
				try {
					
					IType[] siblings = cloneUnit.getTypes();
					
					if (siblings.length == 0) cloneUnit.createType(source, null, false, null);
					else cloneUnit.createType(source, siblings[0], false, null);
				}
				catch (Exception e) {}			
			}
		}
		
		// import insertion ========================================
		
		// Account for the extra space added when the first import statement is introduced to a document.
		if (cloneUnit.getImports().length == 0 && newImports.size() != 0) lastLine++;

		// Update the lastLine variable based on the number of new import statements.
		for (String newImport : newImports) {
			cloneUnit.createImport(newImport, null, null);
			lastLine++;
		}
		
		// Get the affected lines by comparing the old document with the one with the working copy.
		
		IDocument importEditDoc = new Document(cloneUnit.getBuffer().getContents());
		
		int startLinesIn;
		
		for (startLinesIn = 0; startLinesIn < doc.getNumberOfLines(); startLinesIn++) {
			
			String oldLine = doc.get(doc.getLineOffset(startLinesIn),
					doc.getLineLength(startLinesIn));
			
			String newLine = importEditDoc.get(
					importEditDoc.getLineOffset(startLinesIn),
					importEditDoc.getLineLength(startLinesIn));
			
			if (!oldLine.equals(newLine)) break;
		}
		
		int endLinesIn;
		
		for (endLinesIn = 0; endLinesIn < doc.getNumberOfLines(); endLinesIn++) {
			
			String oldLine = doc.get(
					doc.getLineOffset(doc.getNumberOfLines() - endLinesIn - 1),
					doc.getLineLength(doc.getNumberOfLines() - endLinesIn - 1));
			
			String newLine = importEditDoc.get(
					importEditDoc.getLineOffset(
							importEditDoc.getNumberOfLines() - endLinesIn - 1),
					importEditDoc.getLineLength(
							importEditDoc.getNumberOfLines() - endLinesIn - 1));
			
			if (!oldLine.equals(newLine)) {
				endLinesIn--;
				break;
			}
		}

		/* Create another text edit that inserts all the helper classes and import statements,
		 * and add the edit to the final edit.
		 */
		if (startLinesIn <= doc.getNumberOfLines() - endLinesIn) {

			if (dummyPackage != null) srcStart = importEditDoc.getLineOffset(startLinesIn + 1);
			else srcStart = importEditDoc.getLineOffset(startLinesIn);
			
			srcEnd = importEditDoc.getLineOffset(importEditDoc.getNumberOfLines() - endLinesIn);
			
			destStart = doc.getLineOffset(startLinesIn);
			destEnd = doc.getLineOffset(doc.getNumberOfLines() - endLinesIn);
	
			ReplaceEdit helperImportEdit = new ReplaceEdit(destStart, destEnd - destStart,
					importEditDoc.get(srcStart, srcEnd - srcStart));
			
			helperImportEdit.copy().apply(workingDoc);
			finalEdit.addChild(helperImportEdit);
		}
		
		// search box padding =====================================
		
		/* In the case where the user initiates a search near the end of a document, the text editor cannot scroll
		 * past the end, causing problems for search box positioning. To fix this, we add temporary spaces to the
		 * end of the document. This is only done during previews.
		 */
		
		if (!confirmed) {
			
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < searchBoxHeight / (styledText.getLineHeight() + styledText.getLineSpacing()); i++) {
				sb.append('\n');
			}
			
			finalEdit.addChild(new InsertEdit(doc.getLength(), sb.toString()));
		}
		
		// apply edits ============================================

		// Apply all the code changes in one big edit, so we can use one UNDO operation to reset everything.
		unit.applyTextEdit(finalEdit, null);
		unit.reconcile(ICompilationUnit.NO_AST, false, null, null);

		// cursor =================================================
		
		if (confirmed) {

			// Set the cursor to the desired location if this is not a preview.

			int cursorPos = -1;

			int cursorMarkerPos = workingDoc.get().indexOf(CURSOR_MARKER);
			if (cursorMarkerPos != -1) cursorPos = cursorMarkerPos;

			// If no cursor position has been specified, use the end of the snippet.
			int endMarkerPos = workingDoc.get().lastIndexOf(END_MARKER);
			if (endMarkerPos != -1 && cursorPos == -1) cursorPos = endMarkerPos;
			
			editor.selectAndReveal(cursorPos, 0);
		}
		else {
			
			// Obtain a virtual text region representing the lines covered by the search result listings.
			int resultsStart = doc.getLineOffset(lastLine);
			int resultsEnd = doc.getLineOffset(lastLine + searchBoxHeight / (styledText.getLineHeight() + styledText.getLineSpacing()));

			// Direct manipulation of the scroll bar proved unsuccessful, so we select code to indirectly affect the scroll bar.
			
			// Try to show the entire inserted snippet, but give priority to the search result listings.
			editor.selectAndReveal(codeEdit.getOffset(), codeEdit.getLength());
			editor.selectAndReveal(resultsStart, resultsEnd - resultsStart);
			editor.selectAndReveal(codeEdit.getOffset(), 0);  // Make sure nothing is highlighted.
		}
		
		/* Next, we access the text editor's projection annotation model, so we can take into account which
		 * methods / classes have been folded. This makes the visual coordinates we get from the StyledText consistent
		 * with the line offsets we get from the IDocument.
		 */
		
		ProjectionViewer viewer = (ProjectionViewer) editor.getViewer();
		ProjectionAnnotationModel annotModel = viewer.getProjectionAnnotationModel();

		@SuppressWarnings("unchecked")
		Iterator<ProjectionAnnotation> annots = annotModel.getAnnotationIterator();
		
		while (annots.hasNext()) {
			
			ProjectionAnnotation annot = annots.next();
			Position pos = annotModel.getPosition(annot);
			int annotStartLine = doc.getLineOfOffset(pos.offset);
			int annotEndLine = doc.getLineOfOffset(pos.offset + pos.length);
			
			if (annot.isCollapsed() && annotEndLine < lastLine) {
					
				lastLine -= annotEndLine - annotStartLine - 1;
			}
		}
	}

	@Override
	protected String evaluateFormulaSnippetNode(FormulaSnippetNode formNode,
			HashMap<String, String> variables, EffectMatchNode effectNode) {
		
		// Here we evaluate a single snippet formula into a string.
		
		String result = "";
		
		// Nullary formulas...
		if (formNode.numArguments() == 0) {
			
			String varValue = variables.get(formNode.getName());

			if (varValue != null) {

				// If the formula's name is the name of a snippet variable, return the value of that variable.
				result = varValue;
			}
			else if (formNode.getName().equals("cursor")) {

				result = CURSOR_MARKER;
			}
			else if (formNode.getName().equals("dollar")) {

				result = "$";
			}
			else if (formNode.getName().equals("iter")) {
				
				// Find a free name for an iterator. e.g., "i", "j", etc...

				final ArrayList<String> completions = new ArrayList<String>();

				try {

					String testString = "/**//**/";
					
					// If we haven't already, use Code Assist to obtain a listing of all visible variables in the code.
					
					if (!codeAssistCache.containsKey(testString)) {
						
						final ArrayList<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
						
						ICompilationUnit cloneUnit = unit.getWorkingCopy(null);
						cloneUnit.getBuffer().setContents(originalText);
						
						cloneUnit.applyTextEdit(new ReplaceEdit(
								selOffset, selLength, testString), null);
	
						cloneUnit.codeComplete(selOffset + testString.length() - 4,
						new CompletionRequestor() {
							
							@Override
							public void accept(CompletionProposal proposal) {
								
								proposals.add(proposal);
							}
						});
						
						codeAssistCache.put(testString, proposals);
						
						cloneUnit.discardWorkingCopy();
					}
					
					// Limit results to names of fields, local variables, and methods.
					
					for (CompletionProposal proposal : codeAssistCache.get(testString)) {
						
						String completion = new String(proposal.getCompletion());

						switch (proposal.getKind()) {
						
						case CompletionProposal.FIELD_REF:
						case CompletionProposal.LOCAL_VARIABLE_REF:
							
							completions.add(completion);
							break;
							
						case CompletionProposal.METHOD_REF:

							int end = completion.indexOf("(");
							if (end == -1) end = completion.length();
							completions.add(completion.substring(0, end));
							break;
						}
					}
				}
				catch (JavaModelException e) {
					e.printStackTrace();
				}
				
				// Find a free iterator name starting from "i".
	
				char iter = 'i';
				
				while (true) {
					
					if (!variables.containsValue("" + iter) &&
							!completions.contains("" + iter)) break;

					iter++;
				}

				result = "" + iter;
			}
			else if (formNode.getName().equals("helper")) {
				
				// Signals the beginning of a helper class. Switch parsing mode, and add entry to newHelpers.
				readingHelper = true;
				newHelpers.add(new String[] {"", "", formNode.getEffect().getId()});
			}
			else if (formNode.getName().equals("endHelper")) {
				
				// Signals the end of a helper class. Reset parsing mode.
				readingHelper = false;
			}
			else if (formNode.getName().equals("id")) {

				// Return the ID of the current effect.
				result = formNode.getEffect().getId();
			}
			else {

				// Can't find anything else suitable, so just return a blank.
				result = "";
			}
		}
		// Unary formulas...
		else {
			
			if (formNode.getName().equals("import")) {
				
				// Add a new import statement using the first formula argument as the import name.
				if (!unit.getImport(formNode.getArgument(0)).exists()) {
					newImports.add(formNode.getArgument(0));
				}
			}
			else if (formNode.getName().equals("elemType")) {

				// Evaluates to the element type of the loopable whose name is the first formula argument.
				result = getElementType(variables.get(formNode.getArgument(0)));
			}
			else if (formNode.getName().equals("snipType")) {

				// Evaluates to the minor type of the current snippet.
				result = effectNode.getEffect().getMinorType();
			}
			else if (formNode.getName().equals("freeName")) {

				String desiredName = formNode.getArgument(0);
				final ArrayList<String> completions = new ArrayList<String>();

				try {

					String testString = "/**/" + desiredName + "/**/";
					
					if (!codeAssistCache.containsKey(testString)) {
						
						final ArrayList<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
						
						ICompilationUnit cloneUnit = unit.getWorkingCopy(null);
						cloneUnit.getBuffer().setContents(originalText);
						
						cloneUnit.applyTextEdit(new ReplaceEdit(
								selOffset, selLength, testString), null);
	
						cloneUnit.codeComplete(selOffset + testString.length() - 4,
						new CompletionRequestor() {
							
							@Override
							public void accept(CompletionProposal proposal) {
								
								proposals.add(proposal);
							}
						});
						
						cloneUnit.discardWorkingCopy();
						
						codeAssistCache.put(testString, proposals);
					}
					
					for (CompletionProposal proposal : codeAssistCache.get(testString)) {

						String completion = new String(proposal.getCompletion());

						switch (proposal.getKind()) {
						
						case CompletionProposal.FIELD_REF:
						case CompletionProposal.LOCAL_VARIABLE_REF:
							
							completions.add(completion);
							break;
							
						case CompletionProposal.METHOD_REF:

							completions.add(completion.substring(0,
									completion.indexOf("(")));
							break;
						}
					}
				}
				catch (JavaModelException e) {
					e.printStackTrace();
				}

				String currentName = desiredName;
				int suffix = 2;
				
				while (true) {
					
					if (!variables.containsValue(currentName) &&
							!completions.contains(currentName)) break;

					currentName = desiredName + suffix;
					suffix++;
				}

				result = currentName;
			}
		}

		if (readingHelper) {
			
			newHelpers.get(newHelpers.size() - 1)[1] += result;
			return "";
		}
		else return result;
	}

	@Override
	protected String evaluateTextSnippetNode(TextSnippetNode textNode,
			HashMap<String, String> variables, EffectMatchNode effectNode) {
		
		if (readingHelper) {
			
			newHelpers.get(newHelpers.size() - 1)[1] += textNode.getText();
			return "";
		}
		else return textNode.getText();
	}
	
	private String getFreeHelperName(String desiredName, String desiredId,
			ICompilationUnit parentElement) throws Exception {
		
		String currentName = desiredName;
		boolean available = false;
		int suffix = 2;
		
		while (!available) {
			
			available = true;
			
			for (IType type : parentElement.getTypes()) {
				
				String source = type.getSource();
				
				int idStart = source.indexOf("// Snippet ID: ") + 15;
				int idEnd = source.indexOf("\r", idStart);
				int classStart = source.indexOf("class");
				
				if (idStart < 0 || idEnd < 0 || classStart < idStart) continue;

				String id = source.substring(idStart, idEnd);

				if (type.getElementName().equals(currentName)) {
					
					if (id.equals(desiredId)) return null;

					currentName = desiredName + (suffix);
					available = false;
					break;
				}
			}
		}
		
		return currentName;
	}

	private String getVariableType(final String varName) {
		
		final ArrayList<String> varTypes = new ArrayList<String>();

		try {

			String argTest = "/**/" + varName + "/**/";

			if (!codeAssistCache.containsKey(argTest)) {

				final ArrayList<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
			
				ICompilationUnit cloneUnit = unit.getWorkingCopy(null);
				cloneUnit.getBuffer().setContents(originalText);
				
				int testOffset = selOffset + argTest.length() - 4;
				
				cloneUnit.applyTextEdit(new ReplaceEdit(
						selOffset, selLength, argTest), null);
	
				cloneUnit.codeComplete(testOffset, new CompletionRequestor() {
					
					@Override
					public void accept(CompletionProposal proposal) {
	
						proposals.add(proposal);
					}
				});
				
				cloneUnit.discardWorkingCopy();
				
				codeAssistCache.put(argTest, proposals);
			}
			
			for (CompletionProposal proposal : codeAssistCache.get(argTest)) {

				String completion = new String(proposal.getCompletion());
				if (!completion.equals(varName)) continue;
				
				if (proposal.getSignature() != null) {
					
					String signature = new String(proposal.getSignature());
					String finalType = "";

					int argListEnd = signature.indexOf(')');
					if (argListEnd != -1) signature = signature.substring(argListEnd + 1);

					if (signature.startsWith("[")) {
						finalType = "[]";
						signature = signature.substring(1);
					}
	
					if (signature.startsWith("L") && signature.endsWith(";")) {
						finalType = signature.substring(1, signature.length() - 1) + finalType;
						varTypes.add(finalType);
					}
					else if (signature.length() == 1) {
						
						switch (signature.charAt(0)) {

							case 'Z': finalType = "boolean" + finalType; break;
							case 'B': finalType = "byte" + finalType; break;
							case 'C': finalType = "char" + finalType; break;
							case 'S': finalType = "short" + finalType; break;
							case 'I': finalType = "int" + finalType; break;
							case 'J': finalType = "long" + finalType; break;
							case 'F': finalType = "float" + finalType; break;
							case 'D':finalType = "double" + finalType; break;
						}
						
						varTypes.add(finalType);
					}
					else {
						
						varTypes.add(signature);
					}
				}
				else {

					varTypes.add(completion);
				}
			}
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}

		if (varTypes.isEmpty()) varTypes.add("Object");

		if (varTypes.get(0).equals("java.lang.String")) return "String";
		else if (varTypes.get(0).equals("java.lang.Object")) return "Object";
		else return varTypes.get(0);
	}

	private String getElementType(final String loopableName) {

		final ArrayList<String> elemTypes = new ArrayList<String>();

		try {

			String argTest = "/**/" + loopableName + "/**/";
			
			if (!codeAssistCache.containsKey(argTest)) {
				
				final ArrayList<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
				
				ICompilationUnit cloneUnit = unit.getWorkingCopy(null);
				cloneUnit.getBuffer().setContents(originalText);
				
				int testOffset = selOffset + argTest.length() - 4;
				
				cloneUnit.applyTextEdit(new ReplaceEdit(
						selOffset, selLength, argTest), null);
	
				cloneUnit.codeComplete(testOffset, new CompletionRequestor() {
					
					@Override
					public void accept(CompletionProposal proposal) {
						
						proposals.add(proposal);
					}
				});
				
				cloneUnit.discardWorkingCopy();
				
				codeAssistCache.put(argTest, proposals);
			}
			
			for (CompletionProposal proposal : codeAssistCache.get(argTest)) {
				
				if (proposal.getRelevance() < 25) continue;

				String completion = new String(proposal.getCompletion());
				if (!completion.equals(loopableName)) continue;
				
				if (proposal.getSignature() != null) {
					
					String signature = new String(proposal.getSignature());

					int argListEnd = signature.indexOf(')');
					if (argListEnd != -1) signature = signature.substring(argListEnd + 1);

					int typeStart = signature.indexOf("[L");
					if (typeStart == -1) typeStart = signature.indexOf("<L");
					
					if (typeStart != -1) {
						int typeEnd = signature.indexOf(";", typeStart);
						elemTypes.add(signature.substring(typeStart + 2, typeEnd));
					}
					else {

						if (signature.length() == 2 && signature.startsWith("[")) {
							
							switch (signature.charAt(1)) {
								case 'Z': elemTypes.add("boolean"); break;
								case 'B': elemTypes.add("byte"); break;
								case 'C': elemTypes.add("char"); break;
								case 'S': elemTypes.add("short"); break;
								case 'I': elemTypes.add("int"); break;
								case 'J': elemTypes.add("long"); break;
								case 'F': elemTypes.add("float"); break;
								case 'D': elemTypes.add("double"); break;
							}
						}
					}
				}
				else {
					
					elemTypes.add(completion);
				}
			}
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}

		if (elemTypes.isEmpty()) elemTypes.add("java.lang.Object");
		return getPracticalTypeName(elemTypes.get(0));
	}
	
	private String getPracticalTypeName(String canonicalTypeName) {
		
		if (canonicalTypeName.endsWith("[]"))
			return getPracticalTypeName(canonicalTypeName.substring(0,
					canonicalTypeName.lastIndexOf("["))) + "[]";
		
		int leftAngle = canonicalTypeName.indexOf("<");
		int rightAngle = canonicalTypeName.lastIndexOf(">");
	
		if (leftAngle != -1 && rightAngle == canonicalTypeName.length() - 1) {

			return getPracticalTypeName(canonicalTypeName.substring(0, leftAngle)) +
			"<" + getPracticalTypeName(canonicalTypeName.substring(leftAngle + 1, rightAngle)) +
			">";
		}

		try {
			return Class.forName(canonicalTypeName).getSimpleName();
		} catch (ClassNotFoundException e) {}
		
		return canonicalTypeName;
	}
}
