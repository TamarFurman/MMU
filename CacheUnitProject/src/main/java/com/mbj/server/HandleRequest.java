package main.java.com.mbj.server;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import main.java.com.mbj.dm.DataModel;
//import main.java.com.mbj.services.CacheUnitController;
//
//import java.io.BufferedInputStream;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.lang.reflect.Type;
//import java.net.Socket;
//import java.net.UnknownHostException;
//
//public class HandleRequest<T> implements Runnable {
//    Socket s;
//    CacheUnitController<T> controller;
//
//    public HandleRequest(Socket s, CacheUnitController<T> controller) {
//        this.s = s;
//        this.controller = controller;
//    }
//
//    @Override
//    public void run() {
//        try {
//            DataOutputStream writer = new DataOutputStream(s.getOutputStream());
//
//            Gson gson = new Gson();
//            DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
//            StringBuilder sb = new StringBuilder();
//            String content = "";
//
//            do {
//                content = in.readUTF();
//                sb.append(content);
//            } while (in.available() != 0);
//
//            content = sb.toString();
//
//            Type requestType = new TypeToken<Request<DataModel<T>[]>>() {
//            }.getType();
//
//            Request<DataModel<T>[]> request = new Gson().fromJson(content, requestType);
//            DataModel<T>[] returnValues;
//
//            String command = request.getHeaders().get("action").toLowerCase();
//
//            boolean ok = true;
//            switch (command) {
//                case "get": {
//                    try {
//                        returnValues = controller.get(request.getBody());
//                        if (returnValues != null) {
//                            ok = true;
//                        }
//                        writer.writeUTF(gson.toJson(returnValues));
//                        writer.flush();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//                case "delete": {
//                    try {
//                        ok = controller.delete(request.getBody());
//                        writer.writeUTF(gson.toJson(ok));
//                        writer.flush();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//                case "update": {
//                    ok = controller.update(request.getBody());
//                    writer.writeUTF(gson.toJson(ok));
//                    writer.flush();
//                    break;
//                }
//                default: {
//                    break;
//                }
//
//            }
//
//
//        } catch (UnknownHostException e) {
//            System.out.println("Exception: " + e);
//        } catch (IOException e) {
//            System.out.println("Exception: " + e);
//        }finally {
//            try {
//                s.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.java.com.mbj.dm.DataModel;
import main.java.com.mbj.services.CacheUnitController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class HandleRequest<T> implements Runnable {

    private final Socket socket;
    private final CacheUnitController<T> controller;

    private Map<String, String> header;

    public HandleRequest(Socket s, CacheUnitController<T> controller) {
        this.socket = s;
        this.controller = controller;
    }

    @Override
    public void run() {
        try (Scanner reader = new Scanner(new InputStreamReader(socket.getInputStream()))) {
            try (PrintWriter pw = new PrintWriter (new OutputStreamWriter(socket.getOutputStream()))) {
                String response="";
                String req = reader.nextLine();
                Type ref = new TypeToken<Request<DataModel<T>[]>>() {}.getType();
                Request<DataModel<T>[]> request = new Gson().fromJson(req, ref);
                header = request.getHeaders();
                boolean success = false;
                switch (header.get("action")) {

                    case "GET": {
                        try {
                            controller.get(request.getBody());
                            if (controller != null) {
                                success = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    case "DELETE": {
                        try {
                            success = controller.delete(request.getBody());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    case "UPDATE": {
                        success = controller.update(request.getBody());
                        break;
                    }

                    case "SHOWSTATS":{
                        response = controller.getStats(); //handleReqest -> controller -> service
                        break;
                    }

                    default:
                        break;
                }

                if(!header.get("action").equals("SHOWSTATS"))
                    response = success ? "Succeeded": "Failed";
                pw.println(response);
                pw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
