package appjavagrpcaudiostream.servidor;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Servidor {
    public static void main(String[] args) throws IOException, InterruptedException {
        int PUERTO = 9000;
        Server servidor = ServerBuilder.forPort(PUERTO).addService(new ServidorImple()).build();

        servidor.start();
        System.out.println("Servidor escuchando en el puerto :" + PUERTO);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                System.out.println("Recibiendo solicitud de apagado...");
                servidor.shutdown();
                System.out.println("Servidor detenido.");
            }
        });

        servidor.awaitTermination();
    }
}
