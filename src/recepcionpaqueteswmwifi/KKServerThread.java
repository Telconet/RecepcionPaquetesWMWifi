/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recepcionpaqueteswmwifi;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
            
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));   //Para enviar datos.
            String datos = entrada.readLine(); 
            
            System.out.println("Se conecto el waspmote con IP: " + socket.getInetAddress().getHostName() + " al puerto local " + socket.getLocalPort());
                      
            
            //string recibidos/enviados
            KnockKnockProtocol kkp = new KnockKnockProtocol(conf.obtenerParametro(Configuracion.SERVIDOR_NTP));
            
            System.out.append(datos + "\n");

            
            /*}
            else{*/
            Mediciones med = kkp.procesarDatos(datos, tipo);

            System.out.println("Datos procesados");
            socket.close();             //ánadido

            if(med != null){

                BaseDeDatos bd = new BaseDeDatos(tipoDB, null, conf.obtenerParametro(Configuracion.IP_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.USUARIO_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.CLAVE_BASE_DE_DATOS),
                                                        Integer.parseInt(conf.obtenerParametro(Configuracion.PUERTO_BD)), 
                                                        conf.obtenerParametro(Configuracion.ORACLE_SID), conf.obtenerParametro(Configuracion.NOMBRE_BASE_DATOS));

                bd.conectar(); //conexion null
                
                
                //Amazon AWS...
                BaseDeDatos bdAWS = new BaseDeDatos(tipoDB, null, conf.obtenerParametro(Configuracion.IP_AWS), 
                                                        conf.obtenerParametro(Configuracion.USUARIO_BASE_DE_DATOS), 
                                                        conf.obtenerParametro(Configuracion.CLAVE_BASE_DE_DATOS),
                                                        Integer.parseInt(conf.obtenerParametro(Configuracion.PUERTO_BD)), 
                                                        conf.obtenerParametro(Configuracion.ORACLE_SID), conf.obtenerParametro(Configuracion.NOMBRE_BASE_DATOS));

                bdAWS.conectar(); //conexion null

                //Para la tabla historica
                bd.insertarRegistro(tipo, med, conf.obtenerParametro(Configuracion.NOMBRE_TABLA_MEDICIONES) + "_" +med.añoMedicion());   //En tabla historica
                
                //Para la tabla de medicion más actual
                if(bd.verificarActualizarRegistro(tipo, med, conf.obtenerParametro(Configuracion.NOMBRE_TABLA_MEDICIONES_ACTUALES)) < 0){
                    System.out.println("Error al actualizar registro");
                }   
                
                if(bdAWS.verificarActualizarRegistro(tipo, med, conf.obtenerParametro(Configuracion.NOMBRE_TABLA_MEDICIONES_ACTUALES)) < 0){
                    System.out.println("Error al actualizar registro (AWS)");
                }   
                
                
                
                bd.cerrar();
                bdAWS.cerrar();
            }
                
               
                //
                /*String id_WM = med.obtenerIdWaspmote();
                
                //WASPMOTE PREGUNTA SI ES HORA DE CALIBRAR el sensor de CO2
                Calendar tiempoActual = Calendar.getInstance();              //
                
                //Hora y minuto actual
                int horaActual = tiempoActual.get(Calendar.HOUR_OF_DAY);
                int minActual = tiempoActual.get(Calendar.MINUTE);
                
                //hora y minuto de calibracion
                int horaCalibracion = RecepcionPaquetesWMWifi.horaCalibracion.get(Calendar.HOUR_OF_DAY);
                int minCalibracion = RecepcionPaquetesWMWifi.horaCalibracion.get(Calendar.MINUTE);
                
                //Vemos si 
                Boolean WMCalibrado = RecepcionPaquetesWMWifi.calibracionWM.get(id_WM);
                
                if(horaActual > horaCalibracion && minActual > minCalibracion && !WMCalibrado.booleanValue()){
                    //Enviamos calibracion...
                    salida.write("calibrar");
                    salida.flush();
                    System.out.println("no-calibrar");
                    
                    //Ya sabemos que el WM fue calibrado por el día
                    RecepcionPaquetesWMWifi.calibracionWM.put(id_WM, Boolean.TRUE);
                }
                else{
                    //enviamos NEG
                    salida.write("no-calibrar");
                    salida.flush();
                    System.out.println("no-calibrar");
                }  
            }
            else{
                System.out.println("Mediciones fueron NULL");
            }
            //}

            System.out.println("Socket " + socket.getLocalPort() + " cerrado");*/
            //this.socket.close();
           // }
        }
        catch(IOException e){
            System.out.println("Error al recibir datos del Waspmote");
            e.printStackTrace();
        }
    }
}
