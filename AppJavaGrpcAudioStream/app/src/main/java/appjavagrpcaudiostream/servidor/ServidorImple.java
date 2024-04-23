package appjavagrpcaudiostream.servidor;

import java.io.InputStream;

import com.google.protobuf.ByteString;
import com.proto.audio.Audio.PeticionDescargarAudio;
import com.proto.audio.Audio.RespuestaChunkAudio;
import com.proto.audio.ServicioAudioGrpc.ServicioAudioImplBase;

import io.grpc.stub.StreamObserver;

public class ServidorImple extends ServicioAudioImplBase{

    @Override
    public void descargarAudio(PeticionDescargarAudio peticion, StreamObserver<RespuestaChunkAudio> streamObserver){
        String archivoNombre = "/"+peticion.getNombre();
        System.out.println("\n\nEnviando archivo: " + peticion.getNombre());

        InputStream inps = ServidorImple.class.getResourceAsStream(archivoNombre);

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int tamano;
        try {
            while ((tamano = inps.read(buffer, 0, bufferSize)) != -1) {
                RespuestaChunkAudio respuesta = RespuestaChunkAudio.newBuilder().setDatos(ByteString.copyFrom(buffer, 0,tamano)).build();
                System.out.print(".");
                streamObserver.onNext(respuesta);
            }
            inps.close();
        } catch (Exception e) {
            System.out.println("No se encontro el archivo : " + archivoNombre);
        }
        streamObserver.onCompleted();
    }

}
