package org.eclipse.recommenders.examples.demo;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

/**
 * Demo outline
 * 
 */
public class MyWizardPage extends WizardPage {

  private Button button;

  private Text text;

  @Override
  public void createControl(final Composite parent) {

    // Strange usage of Composite:
    final Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(null);
    container.setLayoutData(null);

    // container.setLayout(null);
    // container.setLayout(null);
    // container.setLayoutData(null);

    // Strange usage of Text:
    text = new Text(container, SWT.BORDER);

    // text.setLayoutData(null);
    // text.setText("");

    text.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(final ModifyEvent arg0) {
        // usage of 'text' in listener?

      }
    });

    //
    // button = new Button(container, 0);

    // this.setControl() ?

    final IWorkbenchHelpSystem help = PlatformUI.getWorkbench().getHelpSystem();

  }

  protected MyWizardPage() {
    super("");
  }

}
