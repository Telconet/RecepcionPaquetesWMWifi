/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recepcionpaqueteswmwifi;

import com.mysql.jdbc.StringUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo Murillo
 */
public class Mediciones {
    
    private String[] medicion;
    private String[] nombreMediciones;
    int numeroMediciones;
    private String idWaspmote;
    
    String fecha;
    String hora;
    private boolean tiempoProcesado;
    private String servidorNTP;
    
    public static final String SENSOR_CO2 = "CO2";
    public static final String SENSOR_CO = "CO";
    public static final String SENSOR_TEMPC = "TCA";
    public static final String SENSOR_TEMPF= "TFA";
    public static final String SENSOR_HUMEDAD = "HUMA";
    public static final String SENSOR_PRESION_ATMOSFERICA = "PA";
    public static final String SENSOR_MICROFONO = "MCP";
    public static final String SENSOR_POLVO = "DUST";
    public static final String SENSOR_LUMINOSIDAD = "LUM";
    public static final String SENSOR_BATERIA = "BAT";
    public static final String SENSOR_HORA = "DATE";
    public static final String SENSOR_FECHA = "TIME";
    public static final String SENSOR_PH = "PH";
    public static final String SENSOR_OXIGENO_DISUELTO = "DO";
    public static final String SENSOR_ULTRASONIDO = "US";
    public static final String SENSOR_STRING = "STR";
    
    public static final double ERROR_MEDICION = -9999999999999999.99;
    public static final double LIMITE_MEDICION = -50000.0;
    
    
    public static final String[] SENSORES = {SENSOR_CO2, SENSOR_CO, SENSOR_TEMPC, SENSOR_TEMPF, SENSOR_HUMEDAD, SENSOR_PRESION_ATMOSFERICA, SENSOR_MICROFONO,
                                                SENSOR_POLVO, SENSOR_LUMINOSIDAD, SENSOR_BATERIA, SENSOR_HORA, SENSOR_FECHA, SENSOR_PH, SENSOR_OXIGENO_DISUELTO, SENSOR_ULTRASONIDO};
    
    public Mediciones(int numeroMediciones, String idWaspmote, String servidorNTP){
        this.medicion = new String[numeroMediciones];
        this.nombreMediciones = new String[numeroMediciones];
        this.numeroMediciones = 0;
        this.idWaspmote = idWaspmote;
        this.tiempoProcesado = false;
        this.hora = null;
        this.fecha = null;
        this.servidorNTP = servidorNTP;
    }
    
    //Ya que una medicion puede tener 3 valores (ej. el acelerometro), devolvemos
    //el String
    public double obtenerMedicion(int indice, int subindice){
        try{
            String[] mediciontmp = medicion[indice].split(";");
            return Double.parseDouble(mediciontmp[subindice]);            
        }
        catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            return Double.NaN;
        }
    }
    
    public String obtenerIdWaspmote(){
        return this.idWaspmote;
    }
    
    //Obtiene la medicion por nombre
    //Ya que una medicion puede tener 3 valores (ej. acelerometro), devolvemos
    //el String
    public double obtenerMedicion(String nombre, int subindice){
        try{
            int indice = -1;
            int j = 0;
            
            //Si es fecha u hora, debemos usar otras funciones 
            if( nombre.equals("DATE") || nombre.equals("TIME")){
                return Double.NaN;
            }
            
            //Buscamo el indice del nombre
            for(int i = 0; i < nombreMediciones.length; i++){
                //for(j = 0; j < SENSORES.length ; j++){
                if(nombreMediciones[i].equalsIgnoreCase(nombre)){
                    indice = i;
                    break;
                }
            //}
            }
            
            //Fecha/hora necesitan otro manejo.
            if(indice == -1){
                return Double.NaN;              //para comparar usamos Double.IsNaN(valor);
            }
            
            String[] mediciontmp = medicion[indice].split(";");
            return Double.parseDouble(mediciontmp[subindice]); 
        }
        catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            return ERROR_MEDICION;
        }
        catch(NumberFormatException e){
            return ERROR_MEDICION;
        }
    }
    
     /**
      * Obtiene un string dentro del frame
      * @param nombre
      * @param subindice
      * @return 
      */
     public String obtenerString(String nombre, int subindice){
        try{
            int indice = -1;
            int j = 0;
            
            //Si es fecha u hora, debemos usar otras funciones 
            if( nombre.equals("DATE") || nombre.equals("TIME")){
                return null;
            }
            
            //Buscamo el indice del nombre
            for(int i = 0; i < nombreMediciones.length; i++){
                //for(j = 0; j < SENSORES.length ; j++){
                if(nombreMediciones[i].equalsIgnoreCase(nombre)){
                    indice = i;
                    break;
                }
            //}
            }
            
            //Fecha/hora necesitan otro manejo.
            if(indice == -1){
                return null;              //para comparar usamos Double.IsNaN(valor);
            }
            
            String[] mediciontmp = medicion[indice].split(";");
            return mediciontmp[0];
        }
        catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }
        catch(NumberFormatException e){
            return null;
        }
        
    }
    
    //Obtener hora
    public String fechaMedicion(){
        
        if(!tiempoProcesado){
            if(!procesarTiempo()){
                Calendar calendario = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                System.out.println("what");
                
                this.fecha = sdf.format(calendario.getTime());
                //TODO...
                /*ClienteNTP cliente = new ClienteNTP(this.servidorNTP);
                String[] datos = cliente.solicitarTiempo();
                this.fecha = datos[0];
                this.hora = datos[1];
                if(this.fecha != null && this.hora != null){
                    this.tiempoProcesado = true;
                }
                else this.tiempoProcesado = false;*/
            }
        }
        return fecha;            //TODO
        
    }
    
    
    //Obtener fecha
    public String horaMedicion(){
        
        //#TIME:13-01-01# o #TIME:13-01-01-5# / #TIME:13-01-01+5#
        if(!tiempoProcesado){
            if(!procesarTiempo()){
                //No existe tiempo en el paquete. Usamos tiempo del servidor.
                
                Calendar calendario = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                System.out.println("what");
                
                this.hora = sdf.format(calendario.getTime());
                
                this.tiempoProcesado = true;
                /*ClienteNTP cliente = new ClienteNTP(this.servidorNTP);
                String[] datos = cliente.solicitarTiempo();
                this.fecha = datos[0];
                this.hora = datos[1];
                
                if(this.fecha != null && this.hora != null){
                    this.tiempoProcesado = true;
                }
                else this.tiempoProcesado = false;*/
            }
        }
        return hora;            //TODO  
    }
    
    //Devuelve el nombre de la medicion
    public String obtenerNombreMedicion(int indice){
        try{
            
           return nombreMediciones[indice];
            
        }
        catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }
    }
    
    //Agrega mediciones, haciendo el parsing necesario
    public int agregarMedicion(String medicion){
        try{
            String stringLimpio;
            
            //Removemos simbolo de numeral
            stringLimpio = medicion.replace("#", "");
            
            String[] tmp = stringLimpio.split(":");

            this.nombreMediciones[this.numeroMediciones] = tmp[0];
            this.medicion[this.numeroMediciones] = tmp[1];

            //aumentamos el numero de mediciones
            this.numeroMediciones++;
            return 0;
            
        }
        catch(ArrayIndexOutOfBoundsException e){
            try{
                FileHandler handler = new FileHandler("log_errores.log", true);
                Logger log = Logger.getLogger(Class.class.getName());
                log.addHandler(handler);
                log.log(Level.WARNING, e.getMessage(), e);
                return -1;
            }
            catch(Exception w){
                return -1;
            }
        }
    }

    //Contamos presencia de determinado caracter
    private int ocurrenciasCaracter(String str, char caracter){
        char[] strArreglo = str.toCharArray();
        
        int conteo = 0;
        for(int i = 0; i < strArreglo.length; i++){
            if(strArreglo[i] == caracter){
                conteo++;
            }
        }
        return conteo;
    }
    
    //Procesar tiempo
    private boolean procesarTiempo(){
        try{
            int indiceHora = -1;
            int indiceFecha = -1;

            //Buscamos los indices 
            //Buscamo el indice del nombre
            for(int i = 0; i < nombreMediciones.length; i++){

                if(nombreMediciones[i].equalsIgnoreCase("TIME")){
                    indiceHora = i;
                }
                else if(nombreMediciones[i].equalsIgnoreCase("DATE")){
                    indiceFecha = i;
                }
            }

            //DEBEMOS tener el indice para ambos
            if(indiceFecha == -1 || indiceFecha == 1){
                return false;
            }

            //extraemos la fecha, hora y zona horaria.

            //Fecha
            //#DATE:14-11-08#
            int año = 1970;
            int mes = 0;
            int dia = 1;
            
            int hora = 0;
            int minuto = 0; 
            int segundo = 0;
            
            //Fecha
            String[] valor = medicion[indiceFecha].split("-");                      //Separamos... el segundo item contiene la hora. #TIME y 18-14-14#
            
            año = Integer.parseInt("20" + valor[0]);
            mes = Integer.parseInt(valor[1]);
            dia = Integer.parseInt( valor[2]);
            
            //Hora y zona horaria
            int zonaHoraria = 0;                                                    //GMT por defecto
            if(medicion[indiceHora].contains("+")){                                 //hora + zone
                valor = medicion[indiceHora].split("\\+");                          //13-01-01 y 5
                zonaHoraria = Integer.parseInt(valor[1]);                           //obtenemos zona horaria
                
                valor = valor[0].split("-");                                        //13 01 01

                hora = Integer.parseInt(valor[0]);
                minuto = Integer.parseInt(valor[1]);
                segundo = Integer.parseInt( valor[2]);
            }
            else if(ocurrenciasCaracter(medicion[indiceHora], '-') == 3){           //hora - ZONa

                valor = medicion[indiceHora].split("-");                            //13 01 01 5
                zonaHoraria = -1*Integer.parseInt(valor[3]);                  

                hora = Integer.parseInt(valor[0]);
                minuto = Integer.parseInt(valor[1]);
                segundo = Integer.parseInt( valor[2]);
            }
            else{          
                valor = medicion[indiceHora].split("-");                                    //13-01-01  
                hora = Integer.parseInt(valor[0]);
                minuto = Integer.parseInt(valor[1]);
                segundo = Integer.parseInt( valor[2]);
            }

            //TODO: ... crear objeto calendario...
            Calendar calendario;
            
            calendario = new GregorianCalendar(año, mes, dia, hora, minuto, segundo);
            System.out.println(calendario.getTime());
            
            //áñadimos la zona horaria.
            calendario.add(Calendar.HOUR, zonaHoraria);
            System.out.println(calendario.getTime());
            
            //Formato
            SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm:ss");
                   
            
            this.fecha = sdfFecha.format(calendario.getTime());
            this.hora = sdfHora.format(calendario.getTime());
            
            return true;

        }
    
        catch(Exception e){
            try{
                FileHandler handler = new FileHandler("log_errores.log", true);
                Logger log = Logger.getLogger(Class.class.getName());
                log.addHandler(handler);
                log.log(Level.WARNING, e.getMessage(), e);
                 this.tiempoProcesado = false;
                return false;
            }
            catch(Exception w){
                this.tiempoProcesado = false;
                return false;
            }
        }
    }
}
