package appjavagrpcaudiostream.cliente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.proto.audio.ServicioAudioGrpc;
import com.proto.audio.Audio.PeticionDescargarAudio;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class Cliente {
    
    public static void main(String[] args) {
        
        String host = "localhost";
        int PUERTO = 9000;
        String nombre;

        ManagedChannel canal = ManagedChannelBuilder.forAddress(host, PUERTO).usePlaintext().build();

        // nombre = "anyma.wav";
        // try {
        //     streamWav(canal, nombre, 44100F);
        // } catch (LineUnavailableException e) {
        //     e.printStackTrace();
        // }

        nombre = "tiesto.mp3";
        ByteArrayInputStream stream = descargarArchivoAudio(canal, nombre);
        try {
            playMP3(stream, nombre);
            stream.close();
        } catch (JavaLayerException | IOException e) {
            e.printStackTrace();
        }

        // nombre = "sample.wav";
        // ByteArrayInputStream stream2 = descargarArchivoAudio(canal, nombre);
        // try {
        //     playWav(stream2, nombre);
        //     stream2.close();
        // } catch (IOException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException e) {
        //     e.printStackTrace();
        // }
        
        
        System.out.println("Apagando");
        canal.shutdown();
      
    
    }

    public static void streamWav(ManagedChannel canal, String nombre, float sampleRate) throws LineUnavailableException{
        AudioFormat formato = new AudioFormat(sampleRate, 16,2,true, false);
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(formato);
        sourceDataLine.open();
        sourceDataLine.start();

        ServicioAudioGrpc.ServicioAudioBlockingStub stub = ServicioAudioGrpc.newBlockingStub(canal);
        PeticionDescargarAudio peticion = PeticionDescargarAudio.newBuilder().setNombre(nombre).build();
        int buffer = 1024;
        stub.descargarAudio(peticion).forEachRemaining(respuesta ->{
            sourceDataLine.write(respuesta.getDatos().toByteArray(), 0, buffer);
            System.out.print(".");
        });
        System.out.println("\n\nRecepcion de audio correcta");
        System.out.println("\nReproduccion terminada");
        sourceDataLine.drain();
        sourceDataLine.close();
    }

    public static ByteArrayInputStream descargarArchivoAudio(ManagedChannel canal, String nombre){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ServicioAudioGrpc.ServicioAudioBlockingStub stub = ServicioAudioGrpc.newBlockingStub(canal);
        PeticionDescargarAudio peticion = PeticionDescargarAudio.newBuilder().setNombre(nombre).build();
        System.out.println("\nRecibiendo audio");
        stub.descargarAudio(peticion).forEachRemaining(respuesta ->{
            try {
                stream.write(respuesta.getDatos().toByteArray());
                System.out.print(".");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("\n\nRecepcion de audio correcta");
        return new ByteArrayInputStream(stream.toByteArray());
    }

    public static void playWav(ByteArrayInputStream inputStream, String nombre) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException{
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        System.out.println("Reproduciendo audio: " + nombre+"\n");
        clip.start();
        Thread.sleep(50000);
        clip.stop();
    }

    public static void playMP3(ByteArrayInputStream inputStream, String nombre) throws JavaLayerException{        
        System.out.println("Reproduciendo audio: " + nombre+"\n");
        Player player = new Player(inputStream);
        player.play();        
    }
}
