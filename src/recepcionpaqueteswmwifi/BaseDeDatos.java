/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recepcionpaqueteswmwifi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo Murillo
 */
public class BaseDeDatos {
    
    public static final int MYSQL_DB = 1;
    public static final int ORACLE_DB = 2;
    public static final int POSTGRE_DB = 3;
    
    private String usuario;
    private String clave;
    private int puertoBD;
    private String IPServidor;
    private int tipoBD;
    private Connection conexion;
    private String sid;
    private String baseDeDatos;
    
    
    public BaseDeDatos(int tipoBD, String rutaErrores, String IP, String usuario, String clave, int puerto, String sid, String BD){
        this.tipoBD = tipoBD;
        this.usuario = usuario;
        this.clave = clave;
        this.IPServidor = IP;
        this.puertoBD = puerto;
        this.sid = sid;
        this.baseDeDatos = BD;
    }
    
    //conectar a la base de datos
    public synchronized int conectar(){
        try{
            StringBuilder url;
            
            switch(this.tipoBD){
                case BaseDeDatos.MYSQL_DB:
                    url = new StringBuilder("jdbc:mysql://");
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                    url.append(this.IPServidor);
                    url.append("/");
                    url.append(this.baseDeDatos);
                    break;
                case BaseDeDatos.ORACLE_DB:
                    url = new StringBuilder("jdbc:oracle:thin:@");
                    Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
                    url.append(IPServidor);
                    url.append(":");
                    url.append(puertoBD);
                    url.append(":");
                    url.append(this.sid);
                    break;
                case BaseDeDatos.POSTGRE_DB:
                    url = new StringBuilder("jdbc:postgresql://");
                    Class.forName("org.postgresql.Driver").newInstance();
                    url.append(this.IPServidor);
                    url.append("/");
                    url.append(baseDeDatos);
                    break;
                default:
                    //TODO escribir a log de errores.
                    return -1;
            }
                  
            //Añadimos el host y la base de datos
            

            //Creamos la conexion
            this.conexion = DriverManager.getConnection(url.toString(), usuario, clave);
            
            //
            if(this.conexion != null){
                return 0;
            }
            else return -1;

        }
        catch(SQLException e){
            try{
                FileHandler handler = new FileHandler("log_db.log", true);
                Logger log = Logger.getLogger(Class.class.getName());
                log.addHandler(handler);
                log.log(Level.WARNING, e.getMessage(), e);
                return -1;
            }
            catch(Exception w){
                return -1;
            }
        }
        catch(Exception e){
            try{
                FileHandler handler = new FileHandler("log_db.log", true);
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
    
    //Cierra la conexion a la base de datos
    public synchronized void cerrar(){
        try{
            if(this.conexion != null){
                this.conexion.close();
            }
        }
        catch(Exception e){
            try{
                FileHandler handler = new FileHandler("log_db.log", true);
                Logger log = Logger.getLogger(Class.class.getName());
                log.addHandler(handler);
                log.log(Level.WARNING, e.getMessage(), e);
                return;
            }
            catch(Exception w){
                return;
            }
        }
    }
    
    public synchronized int verificarActualizarRegistro(int tipoMedicion, Mediciones mediciones, String nombreTabla){
        try{
            
            String tipoStr = null;
        
            switch(tipoMedicion){
                case RecepcionPaquetesWMWifi.WASPMOTE_BOSQUES:
                    tipoStr = "bosque";
                    break;
                case RecepcionPaquetesWMWifi.WASPMOTE_CAMARONERA:
                    tipoStr = "camaronera";
                    break;
                case RecepcionPaquetesWMWifi.WASPMOTE_CIUDAD:
                    tipoStr = "ciudad";
                    break;
                case RecepcionPaquetesWMWifi.WASPMOTE_TEST:
                    tipoStr = "test";
                    break;
                case RecepcionPaquetesWMWifi.WASPMOTE_INUNDACIONES:
                    tipoStr = "inundaciones";
                    break;
                default:
                        return -1;
            }
            
            //Verificar si existe tabla. Si no, la creamos. Cambiar a Mediciones...
            //Creamos la consulta
            /**
             *   CREATE TABLE IF NOT EXISTS `sensorParser` (
                `id` int(11) NOT NULL auto_increment,
                `id_wasp` text character set utf8 collate utf8_unicode_ci,
                `id_secret` text character set utf8 collate utf8_unicode_ci,
                `frame_type` int(11) default NULL,
                `frame_number` int(11) default NULL,
                `sensor` text character set utf8 collate utf8_unicode_ci,
                `value` text character set utf8 collate utf8_unicode_ci,
                `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP,
                `raw` text character set utf8 collate utf8_unicode_ci,
                `parser_type` tinyint(3) NOT NULL default '0',
                PRIMARY KEY  (`id`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;
             */
            if(!existeTabla(nombreTabla)){

                Statement consultaStatement = conexion.createStatement();
                String consultaCreacion = "";
                
                //Todas las consultas son iguales
                //"CREATE TABLE IF NOT EXISTS `sensorParser` (" +
                if(this.tipoBD == MYSQL_DB ){
                    String cola = "`id` int(11) NOT NULL auto_increment," +
                    "`id_wasp` text character set utf8 collate utf8_unicode_ci," +
                    "`id_secret` text character set utf8 collate utf8_unicode_ci," +
                    "`frame_type` int(11) default NULL," +
                    "`frame_number` int(11) default NULL,"+
                    "`sensor` text character set utf8 collate utf8_unicode_ci," +
                    "`value` text character set utf8 collate utf8_unicode_ci," +
                    "`timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP," +
                    "`raw` text character set utf8 collate utf8_unicode_ci," +
                    "`parser_type` tinyint(3) NOT NULL default '0'," +
                    "PRIMARY KEY  (`id`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1";
                    
                    String cabeza = "CREATE TABLE IF NOT EXISTS " + nombreTabla + "(";
                    
                    consultaCreacion = cabeza + cola;
                }
                else if(this.tipoBD == ORACLE_DB){
                    
                    String cola = "`id` number(11) NOT NULL auto_increment," +
                    "`id_wasp` text  character set utf8 collate utf8_unicode_ci," +
                    "`id_secret` text  character set utf8 collate utf8_unicode_ci," +
                    "`frame_type` int(11) default NULL," +
                    "`frame_number` int(11) default NULL,"+
                    "`sensor` text  character set utf8 collate utf8_unicode_ci," +
                    "`value` text  character set utf8 collate utf8_unicode_ci," +
                    "`timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP," +
                    "`raw` text  character set utf8 collate utf8_unicode_ci," +
                    "`parser_type` int(3) NOT NULL default '0'," +
                    "PRIMARY KEY  (`id`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1";
                    
                    String cabeza = "CREATE TABLE IF NOT EXISTS " + nombreTabla + "(";
                    
                }
                else if(this.tipoBD == POSTGRE_DB){
                    
                    String cola = "`id` int(11) NOT NULL auto_increment," +
                    "`id_wasp` CLOB  character set utf8 collate utf8_unicode_ci," +
                    "`id_secret` CLOB  character set utf8 collate utf8_unicode_ci," +
                    "`frame_type` number(11) default NULL," +
                    "`frame_number` number(11) default NULL,"+
                    "`sensor` CLOB  character set utf8 collate utf8_unicode_ci," +
                    "`value` CLOB  character set utf8 collate utf8_unicode_ci," +
                    "`timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP," +
                    "`raw` CLOB  character set utf8 collate utf8_unicode_ci," +
                    "`parser_type` number(3) NOT NULL default '0'," +
                    "PRIMARY KEY  (`id`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1";
                    
                    String cabeza = "CREATE TABLE IF NOT EXISTS " + nombreTabla + "(";
                }
             
                //Ejecutamos la creacion de la tabla
                if(consultaCreacion != null){
                    consultaStatement.execute(consultaCreacion); 
                    consultaStatement.close();                  
                }
                else return -1;
            }
            
            
            /*Creamos la consulta de seleccion, para ver si el waspmote existe en la tabla temporal*/
            /*junto con su medicion*/
            //TODO: ACTUALIZAR DE AQUI HASTA FINAL DE LA FUNCION.
            String consultaExistencia = "SELECT * FROM " + nombreTabla + " WHERE id_wasp = '" + mediciones.obtenerIdWaspmote() +"'";
            
            if(this.conexion == null){
                this.conectar();
            }
            
            
            Statement existenciaStatement = conexion.createStatement();
            
            ResultSet set;
            set = existenciaStatement.executeQuery(consultaExistencia);
            
            if(set.next()){
                //Si es verdad, el registro existe, y debemos actualizar
                String consultaActualizacion = null;
                
                if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_CIUDAD){
                
                    consultaActualizacion = "UPDATE " + nombreTabla + " SET fecha = '" + mediciones.fechaMedicion()+ "', hora = '" +  mediciones.horaMedicion() + "', bateria = " + mediciones.obtenerMedicion(Mediciones.SENSOR_BATERIA, 0)  + 
                                            ", ruido = " + mediciones.obtenerMedicion(Mediciones.SENSOR_MICROFONO, 0) + ", polvo = " + mediciones.obtenerMedicion(Mediciones.SENSOR_POLVO, 0) + 
                                            ", humedad = " + mediciones.obtenerMedicion(Mediciones.SENSOR_HUMEDAD, 0) + ", luminosidad = " + mediciones.obtenerMedicion(Mediciones.SENSOR_LUMINOSIDAD, 0) +  
                                            ", temperatura = " + mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) + " WHERE id_wasp = '" + mediciones.obtenerIdWaspmote() + "'";
                }
                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_INUNDACIONES){
                    consultaActualizacion = "UPDATE " + nombreTabla + " SET fecha = '" + mediciones.fechaMedicion() + "', hora = '" + mediciones.horaMedicion() + 
                                            "',  bateria = " +  mediciones.obtenerMedicion(Mediciones.SENSOR_BATERIA, 0) +
                                            ", nivel_agua = " + mediciones.obtenerMedicion(Mediciones.SENSOR_ULTRASONIDO, 0)  + " WHERE id_wasp = '" + mediciones.obtenerIdWaspmote() + "'";

                }
                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_CAMARONERA){
                    consultaActualizacion = "UPDATE " + nombreTabla + " SET fecha = '" + mediciones.fechaMedicion()+ "', hora = '" + mediciones.horaMedicion() + 
                                        "', bateria = " + mediciones.obtenerMedicion(mediciones.SENSOR_BATERIA, 0)  + ", temperatura = " + mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) +  
                                        ", acidez = " + mediciones.obtenerMedicion(Mediciones.SENSOR_PH, 0) + ", oxigeno_disuelto = " +  mediciones.obtenerMedicion(Mediciones.SENSOR_OXIGENO_DISUELTO, 0) + 
                                        " WHERE id_wasp = '" + mediciones.obtenerIdWaspmote()+ "'";
                }

                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_BOSQUES){
                    consultaActualizacion = "UPDATE " + nombreTabla + " SET fecha = '" + mediciones.fechaMedicion() + "', hora = '" + mediciones.horaMedicion() + 
                                        "', bateria = " +  mediciones.obtenerMedicion(mediciones.SENSOR_BATERIA, 0) + ", temperatura = " + mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) +  
                                        ", humedad = " + mediciones.obtenerMedicion(Mediciones.SENSOR_HUMEDAD, 0) + ", CO2ppm = " + mediciones.obtenerMedicion(Mediciones.SENSOR_CO2, 0) + 
                                        " WHERE id_wasp = '" + mediciones.obtenerIdWaspmote() + "'";
                                    
                }

                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_TEST){

                    consultaActualizacion = "UPDATE " + nombreTabla + " fecha = '" + mediciones.fechaMedicion() +  "', hora = '" + mediciones.horaMedicion() +  
                                        "', bateria = " + mediciones.obtenerMedicion(Mediciones.SENSOR_BATERIA, 0) + ", temperatura = " + mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) + 
                                        ", humedad = " + mediciones.obtenerMedicion(Mediciones.SENSOR_HUMEDAD, 0) + " WHERE id_wasp = '" + mediciones.obtenerIdWaspmote() + "'";
                             
                                      

                }
                
                //Verificamos la conexion
                if(this.conexion == null){
                    this.conectar();
                }

                Statement consultaStatement = conexion.createStatement(); 
                int numerosFilasAfectadas = consultaStatement.executeUpdate(consultaActualizacion);
                consultaStatement.close();
                set.close();
                existenciaStatement.close();
                
                return numerosFilasAfectadas;
                
            }
            else{
                //si no existe, insertamos directamente.
                 String consultaInsercion = null;
                
                if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_CIUDAD){
                
                    consultaInsercion = "INSERT INTO " + nombreTabla + " (fecha, hora, id_wasp, bateria, ruido, polvo, humedad, luminosidad, temperatura) VALUES ('" + 
                                      mediciones.fechaMedicion() + "', '" + mediciones.horaMedicion() + "', '" +
                                      mediciones.obtenerIdWaspmote() + "', " + mediciones.obtenerMedicion(Mediciones.SENSOR_BATERIA, 0) + ", " + 
                                      mediciones.obtenerMedicion(Mediciones.SENSOR_MICROFONO, 0) + ", " + mediciones.obtenerMedicion(Mediciones.SENSOR_POLVO, 0) + ", " +
                                      mediciones.obtenerMedicion(Mediciones.SENSOR_HUMEDAD, 0) + ", " + mediciones.obtenerMedicion(Mediciones.SENSOR_LUMINOSIDAD, 0) + ", " +
                                      mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) + ")";

                }
                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_INUNDACIONES){
                    consultaInsercion = "INSERT INTO " + nombreTabla + " (fecha, hora, id_wasp, bateria, nivel_agua) VALUES ('" + 
                                      mediciones.fechaMedicion() + "', '" + mediciones.horaMedicion() + "', '" +
                                      mediciones.obtenerIdWaspmote() + "', " +  mediciones.obtenerMedicion(Mediciones.SENSOR_BATERIA, 0) + "," +
                                      mediciones.obtenerMedicion(Mediciones.SENSOR_ULTRASONIDO, 0) + ")";

                }
                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_CAMARONERA){
                    consultaInsercion = "INSERT INTO " + nombreTabla + " (fecha, hora, id_wasp, bateria, temperatura, acidez, oxigeno_disuelto) VALUES ('" + 
                                      mediciones.fechaMedicion() + "', '" + mediciones.horaMedicion() + "', '" +
                                      mediciones.obtenerIdWaspmote() + "', " + mediciones.obtenerMedicion(mediciones.SENSOR_BATERIA, 0) + ", " +
                                      mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) + ", " + mediciones.obtenerMedicion(Mediciones.SENSOR_PH, 0) +  ", " + 
                                      mediciones.obtenerMedicion(Mediciones.SENSOR_OXIGENO_DISUELTO, 0) + ")";
                }

                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_BOSQUES){
                    consultaInsercion = "INSERT INTO " + nombreTabla + " (fecha, hora, id_wasp, bateria, temperatura, humedad, CO2ppm) VALUES ('" + 
                                      mediciones.fechaMedicion() + "', '" + mediciones.horaMedicion() + "', '" +
                                      mediciones.obtenerIdWaspmote() + "', " + mediciones.obtenerMedicion(mediciones.SENSOR_BATERIA, 0) + ", " +
                                      mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) + ", " + mediciones.obtenerMedicion(Mediciones.SENSOR_HUMEDAD, 0) +  ", " + 
                                      mediciones.obtenerMedicion(Mediciones.SENSOR_CO2, 0) + ")";
                }

                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_TEST){

                    consultaInsercion = "INSERT INTO " + nombreTabla + " (fecha, hora, id_wasp, bateria, temperatura, humedad) VALUES ('" + 
                                      mediciones.fechaMedicion() + "', '" + mediciones.horaMedicion() + "', '" +
                                      mediciones.obtenerIdWaspmote() + "', " + mediciones.obtenerMedicion(Mediciones.SENSOR_BATERIA, 0) + ", " +
                                      mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) + ", " + mediciones.obtenerMedicion(Mediciones.SENSOR_HUMEDAD, 0) +  ")";

                }
                
                
                if(this.conexion == null){
                    this.conectar();
                }

                Statement consultaStatement = conexion.createStatement(); 
                int numerosFilasAfectadas = consultaStatement.executeUpdate(consultaInsercion);
                consultaStatement.close();
                set.close();
                existenciaStatement.close();

                return numerosFilasAfectadas;
            }
            
           
        }
        catch(Exception e){
            try{
                FileHandler handler = new FileHandler("log_db.log", true);
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
    
    //Almancenar mediciones...
    public synchronized int insertarRegistro(int tipoMedicion, Mediciones mediciones, String nombreTabla){
        
        String tipoStr = null;
        
        switch(tipoMedicion){
            case RecepcionPaquetesWMWifi.WASPMOTE_BOSQUES:
                tipoStr = "bosque";
                break;
            case RecepcionPaquetesWMWifi.WASPMOTE_CAMARONERA:
                tipoStr = "camaronera";
                break;
            case RecepcionPaquetesWMWifi.WASPMOTE_CIUDAD:
                tipoStr = "ciudad";
                break;
            case RecepcionPaquetesWMWifi.WASPMOTE_TEST:
                tipoStr = "test";
                break;
            case RecepcionPaquetesWMWifi.WASPMOTE_INUNDACIONES:
                tipoStr = "inundaciones";
                break;
            default:
                    return -1;
        }
        
        try{
            
            String consultaInsercion = null;
            
            
            //
            
            
            if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_CIUDAD){
               
                
                
                
                consultaInsercion = "INSERT INTO " + nombreTabla + " (fecha, hora, id_wasp, bateria, ruido, polvo, humedad, luminosidad, temperatura) VALUES ('" + 
                                  mediciones.fechaMedicion() + "', '" + mediciones.horaMedicion() + "', '" +
                                  mediciones.obtenerIdWaspmote() + "', " + mediciones.obtenerMedicion(Mediciones.SENSOR_BATERIA, 0) + ", " + 
                                  mediciones.obtenerMedicion(Mediciones.SENSOR_MICROFONO, 0) + ", " + mediciones.obtenerMedicion(Mediciones.SENSOR_POLVO, 0) + ", " +
                                  mediciones.obtenerMedicion(Mediciones.SENSOR_HUMEDAD, 0) + ", " + mediciones.obtenerMedicion(Mediciones.SENSOR_LUMINOSIDAD, 0) + ", " +
                                  mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) + ")";
                
            }
            else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_INUNDACIONES){
                consultaInsercion = "INSERT INTO " + nombreTabla + " (fecha, hora, id_wasp, bateria, nivel_agua) VALUES ('" + 
                                  mediciones.fechaMedicion() + "', '" + mediciones.horaMedicion() + "', '" +
                                  mediciones.obtenerIdWaspmote() + "', " +  mediciones.obtenerMedicion(Mediciones.SENSOR_BATERIA, 0) + "," +
                                  mediciones.obtenerMedicion(Mediciones.SENSOR_ULTRASONIDO, 0) + ")";

            }
            else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_CAMARONERA){
                consultaInsercion = "INSERT INTO " + nombreTabla + " (fecha, hora, id_wasp, bateria, temperatura, acidez, oxigeno_disuelto) VALUES ('" + 
                                  mediciones.fechaMedicion() + "', '" + mediciones.horaMedicion() + "', '" +
                                  mediciones.obtenerIdWaspmote() + "', " + mediciones.obtenerMedicion(mediciones.SENSOR_BATERIA, 0) + ", " +
                                  mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) + ", " + mediciones.obtenerMedicion(Mediciones.SENSOR_PH, 0) +  ", " + 
                                  mediciones.obtenerMedicion(Mediciones.SENSOR_OXIGENO_DISUELTO, 0) + ")";
            }
            
            else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_BOSQUES){
                consultaInsercion = "INSERT INTO " + nombreTabla + " (fecha, hora, id_wasp, bateria, temperatura, humedad, CO2ppm) VALUES ('" + 
                                  mediciones.fechaMedicion() + "', '" + mediciones.horaMedicion() + "', '" +
                                  mediciones.obtenerIdWaspmote() + "', " + mediciones.obtenerMedicion(mediciones.SENSOR_BATERIA, 0) + ", " +
                                  mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) + ", " + mediciones.obtenerMedicion(Mediciones.SENSOR_HUMEDAD, 0) +  ", " + 
                                  mediciones.obtenerMedicion(Mediciones.SENSOR_CO2, 0) + ")";
            }
            
            else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_TEST){
                
                consultaInsercion = "INSERT INTO " + nombreTabla + " (fecha, hora, id_wasp, bateria, temperatura, humedad) VALUES ('" + 
                                  mediciones.fechaMedicion() + "', '" + mediciones.horaMedicion() + "', '" +
                                  mediciones.obtenerIdWaspmote() + "', " + mediciones.obtenerMedicion(Mediciones.SENSOR_BATERIA, 0) + ", " +
                                  mediciones.obtenerMedicion(Mediciones.SENSOR_TEMPC, 0) + ", " + mediciones.obtenerMedicion(Mediciones.SENSOR_HUMEDAD, 0) +  ")";
                
            }

            //Verificar si existe tabla. Si no, la creamos. Cambiar a Mediciones...
            if(!existeTabla(nombreTabla)){

                Statement consultaStatement = conexion.createStatement();
                String consultaCreacion = "";
                
                if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_CIUDAD){
                
                    if(this.tipoBD == MYSQL_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                           "fecha DATE, " +
                                           "hora TIME, " +
                                           "id_wasp VARCHAR(255), " +
                                           "bateria DOUBLE," +
                                           "ruido DOUBLE," + 
                                           "polvo DOUBLE," +
                                           "humedad DOUBLE," +
                                           "luminosidad DOUBLE," + 
                                           "temperatura DOUBLE," + 
                                           "CONSTRAINT FK_id_wasp_" + tipoStr  + "_" + mediciones.añoMedicion() + " FOREIGN KEY (id_wasp) REFERENCES info_waspmotes(id_wasp)) ENGINE=InnoDB";
                    }
                    else if(this.tipoBD == POSTGRE_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                           "fecha DATE, " +
                                           "hora TIME, " +
                                           "id_wasp VARCHAR(255), " +
                                           "bateria REAL," +
                                           "ruido REAL," + 
                                           "polvo REAL," +
                                           "humedad REAL," +
                                           "luminosidad REAL," + 
                                           "temperatura REAL)";
                    }
                    else if(this.tipoBD == ORACLE_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                           "fecha DATE, " +
                                           "hora TIME, " +
                                           "id_wasp VARCHAR(255), " +
                                           "bateria BINARY_FLOAT," +
                                           "ruido BINARY_FLOAT," + 
                                           "polvo BINARY_FLOAT," +
                                           "humedad BINARY_FLOAT," +
                                           "luminosidad BINARY_FLOAT," + 
                                           "temperatura BINARY_FLOAT)";
                    }
                }
                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_INUNDACIONES){
                    if(this.tipoBD == MYSQL_DB){ 
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                           "fecha DATE, " +
                                           "hora TIME, " +
                                           "id_wasp VARCHAR(255), " +
                                           "bateria DOUBLE, " +
                                           "nivel_agua DOUBLE," + 
                                           "CONSTRAINT FK_id_wasp_" + tipoStr  + "_" + mediciones.añoMedicion() + " FOREIGN KEY (id_wasp) REFERENCES info_waspmotes(id_wasp)) ENGINE=InnoDB";
                    }
                    else if(this.tipoBD == POSTGRE_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                           "fecha DATE, " +
                                           "hora TIME, " +
                                           "id_wasp VARCHAR(255), " +
                                           "bateria DOUBLE, " +
                                           "nivel_agua REAL)";
                    }
                    else if(this.tipoBD == ORACLE_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                           "fecha DATE, " +
                                           "hora TIME, " +
                                           "id_wasp VARCHAR(255), " +
                                           "bateria DOUBLE, " +
                                           "nivel_agua BINARY_FLOAT)";
                    }

                }
                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_CAMARONERA){
                    if(this.tipoBD == MYSQL_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                          "fecha DATE, " +
                                          "hora TIME, " +
                                          "id_wasp VARCHAR(255), " +
                                          "bateria DOUBLE," +
                                          "temperatura DOUBLE," +
                                          "acidez DOUBLE," +
                                          "oxigeno_disuelto DOUBLE," + 
                                          "CONSTRAINT FK_id_wasp_" + tipoStr  + "_" + mediciones.añoMedicion() + " FOREIGN KEY (id_wasp) REFERENCES info_waspmotes(id_wasp)) ENGINE=InnoDB";
                    }
                    else if(this.tipoBD == POSTGRE_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                          "fecha DATE, " +
                                          "hora TIME, " +
                                          "id_wasp VARCHAR(255), " +
                                          "bateria REAL," +
                                          "temperatura REAL," +
                                          "acidez REAL," +
                                          "oxigeno_disuelto REAL)";
                    }
                    else if(this.tipoBD == ORACLE_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                          "fecha DATE, " +
                                          "hora TIME, " +
                                          "id_wasp VARCHAR(255), " +
                                          "bateria BINARY_FLOAT," +
                                          "temperatura BINARY_FLOAT," +
                                          "acidez BINARY_FLOAT," +
                                          "oxigeno_disuelto BINARY_FLOAT)";
                    }
                }
                
                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_BOSQUES){
                    if(this.tipoBD == MYSQL_DB){ 
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                           "fecha DATE, " +
                                           "hora TIME, " +
                                           "id_wasp VARCHAR(255), " +
                                           "bateria DOUBLE, " +
                                           "temperatura DOUBLE, " +
                                           "humedad DOUBLE, " +
                                           "CO2ppm DOUBLE," +
                                           "CONSTRAINT FK_id_wasp_" + tipoStr  + "_" + mediciones.añoMedicion() + " FOREIGN KEY (id_wasp) REFERENCES info_waspmotes(id_wasp)) ENGINE=InnoDB";
                    
                    }
                    else if(this.tipoBD == POSTGRE_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                           "fecha DATE, " +
                                           "hora TIME, " +
                                           "id_wasp VARCHAR(255), " +
                                           "bateria DOUBLE, " +
                                           "temperatura REAL," + 
                                           "humedad REAL," + 
                                           "CO2ppm REAL)";
                    }
                    else if(this.tipoBD == ORACLE_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                           "fecha DATE, " +
                                           "hora TIME, " +
                                           "id_wasp VARCHAR(255), " +
                                           "bateria DOUBLE, " +
                                           "temperatura BINARY_FLOAT," +
                                           "humedad BINARY_FLOAT," +
                                           "CO2ppm BINARY_FLOAT)";
                    }

                }
                
                else if(tipoMedicion == RecepcionPaquetesWMWifi.WASPMOTE_TEST){
                
                    if(this.tipoBD == MYSQL_DB){
                        consultaCreacion = "CREATE TABLE " + nombreTabla + " (" + 
                                           "fecha DATE, " +
                                           "hora TIME, " +
                                           "id_wasp VARCHAR(255), " +
                                           "bateria REAL," + 
                                           "temperatura REAL," +
                                           "CO2 REAL," +
                                           "humedad REAL," + 
                                           "CONSTRAINT FK_id_wasp_" + tipoStr  + "_" + mediciones.añoMedicion() + " FOREIGN KEY (id_wasp) REFERENCES info_waspmotes(id_wasp)) ENGINE=InnoDB";
                    }
                }
                
                //Ejecutamos la creacion de la tabla
                if(consultaCreacion != null){
                    consultaStatement.execute(consultaCreacion); 
                    consultaStatement.close();                  
                }
                else return -1;
            }
            
            System.out.println(consultaInsercion);

            //Ejecutamos la consulta de almacenamiento
            if(this.conexion == null){
                this.conectar();
            }
            
            Statement consultaStatement = conexion.createStatement(); 
            int numerosFilasAfectadas = consultaStatement.executeUpdate(consultaInsercion);
            consultaStatement.close();

            return numerosFilasAfectadas;

        }
        catch(Exception e){
            try{
                FileHandler handler = new FileHandler("log_db.log", true);
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
    
    /**
     * Verifica si exite la tabla dada por el nombre
     */
    public synchronized boolean  existeTabla(String nombreTabla){
        try{
            
            if(this.conexion == null){
               this.conectar(); 
            }
            DatabaseMetaData dbm = this.conexion.getMetaData();
            ResultSet tablas = dbm.getTables(null, null, nombreTabla, null);

            if(tablas.next()){
                
                if(tablas != null){   //--CHECK
                    tablas.close();
                }
                return true;
            }
            else{
                if(tablas != null){   //--CHECK
                    tablas.close();
                }
                return false;
            }
        }
        catch(Exception e){
            try{
                FileHandler handler = new FileHandler("log_db.log", true);
                Logger log = Logger.getLogger(Class.class.getName());
                log.addHandler(handler);
                log.log(Level.WARNING, e.getMessage(), e);
                return false;
            }
            catch(Exception w){
                return false;
            }
        }
    }
}
