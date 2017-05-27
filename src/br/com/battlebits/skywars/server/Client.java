package br.com.battlebits.skywars.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Arquivo criado em 27/05/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
public class Client extends Thread
{
    private Socket socket;
    private static long clientCount;

    public Client(Socket socket)
    {
        this.socket = socket;
        setName("Client - #" + (clientCount++));
    }

    @Override
    public void run()
    {
        try
        {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            String utf = dis.readUTF();

            if (utf.startsWith("{") && utf.endsWith("}"))
            {
                JsonElement element = Server.getParser().parse(utf);

                if (element instanceof JsonObject)
                {
                    JsonObject object = (JsonObject) element;

                    if (object.has("action") && object.has("token") && object.get("token").getAsString().equals(Server.TOKEN))
                    {
                        String action = object.get("action").getAsString();

                        switch (action)
                        {
                            case "request":
                            {
                                if (!object.has("type") && !object.has("insane"))
                                {
                                    dos.writeUTF("declined");
                                    break;
                                }

                                String type = object.get("type").getAsString();
                                boolean insane = object.get("insane").getAsBoolean();

                                File folder = new File(String.format(Server.MAP_PATH, type.toLowerCase(), insane ? "insane" : "normal"));

                                if (folder.exists() && folder.isDirectory())
                                {
                                    List<File> files = new ArrayList<>();

                                    for (File file : folder.listFiles())
                                    {
                                        if (file.isFile() && file.getName().endsWith(".dat"))
                                        {
                                            files.add(file);
                                        }
                                    }

                                    if (files.isEmpty())
                                    {
                                        dos.writeUTF("declined");
                                        break;
                                    }

                                    File file = files.get(Server.getRandom().nextInt(files.size()));
                                    String name = file.getName().replace("_", "").replaceFirst(".dat", "");
                                    byte[] buffer = Files.readAllBytes(file.toPath());

                                    dos.writeUTF("accepted");
                                    dos.writeUTF(name);

                                    dos.writeInt(buffer.length);
                                    dos.write(buffer);
                                }
                                else
                                {
                                    dos.writeUTF("declined");
                                }

                                break;
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
