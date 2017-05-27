package br.com.battlebits.skywars.bukkit.data.mongodb;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import br.com.battlebits.commons.core.backend.Backend;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MongoBackend implements Backend {
    @Getter
    private MongoClient client;

    @NonNull
    private final String hostname, database, username, password;
    private final int port;

    public MongoBackend(JsonObject mongodb) {
        this(mongodb.get("host").getAsString(),
                mongodb.get("database").getAsString(),
                mongodb.get("username").getAsString(),
                mongodb.get("password").getAsString(),
                mongodb.get("port").getAsInt());
    }

    @Override
    public void startConnection() {
        List<ServerAddress> addresses = new ArrayList<>();
        addresses.add(new ServerAddress(hostname, port));

        List<MongoCredential> credentials = new ArrayList<>();
        if (!username.isEmpty() && !password.isEmpty() && !database.isEmpty())
            credentials.add(MongoCredential.createMongoCRCredential(username, database, password.toCharArray()));

        client = new MongoClient(addresses, credentials);
    }

    @Override
    public void closeConnection() {
        client.close();
    }

    @Override
    public boolean isConnected() throws Exception {
        return client != null;
    }

    @Override
    public void recallConnection() throws Exception {
        // Nope
    }
}
