/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recepcionpaqueteswmwifi;
import java.io.*;
import java.net.*;


/**
 *
 * @author Eduardo
 */
public class KKServerThread  extends Thread{
    
    private Socket socket = null;
    private int tipo = 0;
    private Configuracion conf = null;

    public KKServerThread(Socket socket, int tipo, Configuracion conf) {
        super("KKMultiServerThread");
        this.socket = socket;
        this.tipo = tipo;
        this.conf = conf;
        System.out.println("Se conecto el waspmote con IP: " + socket.getInetAddress().getHostName());
    }
    
    //Corremos el codigo para comunicarnos con el equipo
    public void run(){
        try{
                        
            //Obtenemos los streams para leer o escribir datos al socket.
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            //string recibidos/enviados
            String lineaEntrada, lineaSalida;
            KnockKnockProtocol kkp = new KnockKnockProtocol();
            
            //procesamos y alamcenamos los datos
            Mediciones med = kkp.procesarDatos(entrada.readLine(), tipo);
            
            if(med != null){
                
                BaseDeDatos bd = new BaseDeDatos(tipo, null, conf.obtenerParametro(Configuracion.IP_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.USUARIO_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.CLAVE_BASE_DE_DATOS),
                                                        Integer.parseInt(conf.obtenerParametro(Configuracion.PUERTO_SERVIDOR)), 
                                                        conf.obtenerParametro(Configuracion.ORACLE_SID));
                
                //TODO...
                bd.conectar("waspmote_test");
                bd.insertarRegistro(tipo, med);
                bd.cerrar();
            }

        }
        catch(IOException e){
            System.out.println("Error al recibir datos del Waspmote");
            e.printStackTrace();
        }

    }
}
