/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recepcionpaqueteswmwifi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Eduardo
 */
public class RecepcionPaquetesWMWifi {

    /**
     * @param args the command line arguments
     */
    
    public static final int WASPMOTE_CIUDAD = 1;
    public static final int NUMERO_MEDICIONES_WM_CIUDAD = 8;
    public static final int WASPMOTE_CAMARONERA = 2;
    public static final int NUMERO_MEDICIONES_WM_CAMARONERA = 3;        //verificar
    public static final int WASPMOTE_INUNDACIONES = 3;
    public static final int NUMERO_MEDICIONES_WM_INUNDACIONES = 2;      //verificar
    
    
    //java -jar programa <tipo> <puerto de escucha> <bd> <ruta archivo>
    //<bd>:     mysql, postgres, oracle
    //<puerto>: 0 a 65535
    //<tipo>:   inundaciones, ciudad, camaronera
    //<ruta archivo>: archivo con informacion de BD.
            
    public static void main(String[] args) {
        // TODO code application logic here
        
        //El primer numero de puerto
        try{
            
            //test
            String datos = "<=>\u0080\u0008#34543#BOSQUE_1#12#BAT:91#MCP:85.3#DUST:0.08258#TCA:23.4#HUMA:33.3#LUM:56.77#DATE:14-11-25#TIME:00-49-52+5#";  //confirmado formato hora/fecha
            
            System.out.println("tamaño paquete: " + datos.length());            
            char[] arreglo = datos.toCharArray();
            byte numero = (byte)arreglo[3];

            System.out.println(datos);
            
            KnockKnockProtocol kkp = new KnockKnockProtocol();            
            Mediciones med = kkp.procesarDatos(datos, WASPMOTE_CIUDAD);
            
            String ruta = args[3];
            Configuracion conf = new Configuracion(ruta);
            
             if(med != null){
                
                /*BaseDeDatos bd = new BaseDeDatos(BaseDeDatos.POSTGRE_DB, null, conf.obtenerParametro(Configuracion.IP_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.USUARIO_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.CLAVE_BASE_DE_DATOS),
                                                        Integer.parseInt(conf.obtenerParametro(Configuracion.PUERTO_SERVIDOR)));*/
                //para postgres
                BaseDeDatos bd = new BaseDeDatos(BaseDeDatos.POSTGRE_DB, null, conf.obtenerParametro(Configuracion.IP_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.USUARIO_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.CLAVE_BASE_DE_DATOS),
                                                        5432, "ed");
                
                bd.conectar(conf.obtenerParametro(Configuracion.NOMBRE_BASE_DATOS));
                //TODO...
                bd.insertarRegistro(WASPMOTE_CIUDAD, med);
                bd.cerrar();
            }
           
            //fin de test            
            if(args.length < 4){
                System.out.println("Numero insuficiente de argumentos");
                System.exit(-1);
            }
            
            //Verificar opciones validas
            int puerto  = Integer.parseInt(args[1]);
            int tipo = 0;
            
            if(puerto > 65535 ){
                System.out.println("Numero de puerto inválido. El programa se cerrará.");
                System.exit(-1);
            }
            
            if(args[0].toLowerCase().contains("inundaciones")){
                tipo = WASPMOTE_INUNDACIONES;
            }
            else if(args[0].toLowerCase().contains("ciudad")){
                tipo = WASPMOTE_CIUDAD;
            }
            else if(args[0].toLowerCase().contains("camaronera")){
                tipo = WASPMOTE_CAMARONERA;
            }
            else{
                System.out.println("Opcion de tipo de waspmote inválida.");
                System.exit(-1);
            }
            
            //Tipo de base de datos
            int tipoDB = -1;
            if(!(args[2].equals("mysql") || args[2].equals("postgres") || args[2].equals("oracle") )){
                System.out.println("Nombre de base de datos inválido. El programa se cerrará.");
                System.exit(-1);
            }
            else if(args[2].equals("mysql")){
                tipoDB = BaseDeDatos.MYSQL_DB;
            }
            else if(args[2].equals("oracle")){
                tipoDB = BaseDeDatos.ORACLE_DB;
            }
            else if(args[2].equals("postgres")){
                tipoDB = BaseDeDatos.POSTGRE_DB;
            }
            
            //Abrimos archivo configuracion
            //String ruta = args[3];
            //Configuracion conf = new Configuracion(ruta);
            
            /*String tmp = conf.obtenerParametro(Configuracion.CLAVE_BASE_DE_DATOS);
            tmp = conf.obtenerParametro(Configuracion.IP_BASE_DE_DATOS);
            tmp = conf.obtenerParametro(Configuracion.NOMBRE_BASE_DATOS);
            tmp = conf.obtenerParametro(Configuracion.PUERTO_SERVIDOR);
            tmp = conf.obtenerParametro(Configuracion.CLAVE_BASE_DE_DATOS);*/
            
            
            //Creamos el socket
            ServerSocket socketServidor = new ServerSocket(puerto);
            boolean escuchar = true;
            
            //Aceptamos conexiones de clientes...
            while(escuchar){
               new KKServerThread(socketServidor.accept(), tipo, conf).start();
            }
            
            socketServidor.close();
        }
        catch(NumberFormatException bad){
            System.out.println("El puerto no es un número válido. Este programa se cerrará.\n");
            System.exit(-1);
        }
        catch(Exception e){
            
        }
    }
}
