package org.eclipse.recommenders.snipmatch.search;

public abstract class ClientSwitcher {
	protected SearchClient client;
	
	public void setSearchClient(SearchClient c){
		client = c;
	}
}
