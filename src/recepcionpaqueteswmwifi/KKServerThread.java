/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recepcionpaqueteswmwifi;
import java.io.*;
import java.net.*;

/**
 *
 * @author Eduardo Murillo
 */
public class KKServerThread  extends Thread{
    
    private Socket socket = null;
    private int tipo = 0;
    private int tipoDB = 0;
    private Configuracion conf = null;

    public KKServerThread(Socket socket, int tipo, int tipoDB, Configuracion conf) {
        super("KKMultiServerThread");
        this.socket = socket;
        this.tipo = tipo;
        this.conf = conf;
        this.tipoDB = tipoDB;
        
    }
    
    //Corremos el codigo para comunicarnos con el equipo
    public void run(){
        try{
            
            System.out.println("Se conecto el waspmote con IP: " + socket.getInetAddress().getHostName() + " al puerto local " + socket.getLocalPort());
                        
            //Obtenemos los streams para leer o escribir datos al socket.
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            //string recibidos/enviados
            String lineaEntrada, lineaSalida;
            KnockKnockProtocol kkp = new KnockKnockProtocol(conf.obtenerParametro(Configuracion.SERVIDOR_NTP));
            
            //procesamos y alamcenamos los datos
            String datos = entrada.readLine();
            System.out.append(datos + "\n");
            
            /*if(datos.contains("STATUS")){ 
                //TODO... GUARDAR EVENTO
            }
            else if(datos.contains("SYNC_TIEMPO")){
                //TODO... ENVIAR TIEMPO AL WASPOMOTE...
            }
            else{*/
            Mediciones med = kkp.procesarDatos(datos, tipo);

            System.out.println("Datos procesados");

            if(med != null){

                BaseDeDatos bd = new BaseDeDatos(tipoDB, null, conf.obtenerParametro(Configuracion.IP_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.USUARIO_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.CLAVE_BASE_DE_DATOS),
                                                        Integer.parseInt(conf.obtenerParametro(Configuracion.PUERTO_BD)), 
                                                        conf.obtenerParametro(Configuracion.ORACLE_SID), conf.obtenerParametro(Configuracion.NOMBRE_BASE_DATOS));

                bd.conectar(); //conexion null
                bd.insertarRegistro(tipo, med, conf.obtenerParametro(Configuracion.NOMBRE_TABLA_MEDICIONES));
                bd.cerrar();
            }
            else{
                System.out.println("Mediciones fueron NULL");
            }

            System.out.println("Socket " + socket.getLocalPort() + " cerrado");
            //this.socket.close();
           // }
        }
        catch(IOException e){
            System.out.println("Error al recibir datos del Waspmote");
            e.printStackTrace();
        }
    }
}
