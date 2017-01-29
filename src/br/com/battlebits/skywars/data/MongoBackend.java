package br.com.battlebits.skywars.data;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import br.com.battlebits.commons.core.backend.Backend;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MongoBackend implements Backend
{
	private MongoClient client;
	@NonNull
	private final String hostname, database, username, password;
	private final int port;

	public MongoBackend() 
	{
		this("localhost", "", "", "", 27017);
	}

	@Override
	public void startConnection()
	{
		List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		seeds.add(new ServerAddress(hostname, port));
		
		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		
		if (!username.isEmpty() && !password.isEmpty() && !database.isEmpty()) 
		{
			credentials.add(MongoCredential.createMongoCRCredential(username, database, password.toCharArray()));
		}
		
		client = new MongoClient(seeds, credentials);
	}

	@Override
	public void closeConnection()
	{
		client.close();
	}

	public MongoClient getClient()
	{
		return client;
	}

	@Override
	public boolean isConnected() throws Exception
	{
		return client != null;
	}

	@Override
	public void recallConnection() throws Exception {}
}
