package br.com.battlebits.skywars.server;

import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Arquivo criado em 27/05/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
public class Server extends Thread
{
    @Getter
    private static Server instance;

    @Getter
    private static Random random = new Random();

    @Getter
    private static JsonParser parser = new JsonParser();

    public static final int PORT = 64500;
    public static final String TOKEN = "2b7Bbqx9lk7m4JO2W6GBunUwiLvOHh5P";
    public static final String PATH = System.getProperty("user.dir") + File.separator;
    public static final String MAP_PATH = PATH + "%s" + File.separator + "%s";

    public static boolean isRunning()
    {
        return instance != null;
    }

    public static void main(String[] args) throws Exception
    {
        instance = new Server();
        instance.start();
    }

    public Server()
    {
        setName("Server");
    }

    @Override
    public void run()
    {
        try
        {
            try (ServerSocket server = new ServerSocket(PORT))
            {
                while (!server.isClosed())
                {
                    Socket socket = server.accept();
                    new Client(socket).start();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
