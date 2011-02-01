package acme;


public abstract class Wizard
{

    IWizardContainer container;



    public void setWindowTitle(final String title)
    {
        container.updateWindowTitle();
    }
}
