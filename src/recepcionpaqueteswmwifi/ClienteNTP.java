/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recepcionpaqueteswmwifi;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

/**
 *
 * @author Eduardo Murillo
 * Parte de este codigo fue tomado del ejemplo NTPClient
 * incluido con la libreria Apache Commons.
 */
public class ClienteNTP {
    
    private String servidorNTP;
    
    public ClienteNTP(String servidor){
        this.servidorNTP = servidor;
    }
    
    /**
     * Funcion que retorna la fecha y la hora con formato.
     * @return Un arreglo con la fecha (item 0), y la hora (item 1).
     */
    public String[] solicitarTiempo(){
        try{
            NTPUDPClient cliente = new NTPUDPClient();
            
            InetAddress hostAddr = InetAddress.getByName(this.servidorNTP);
            TimeInfo info = cliente.getTime(hostAddr);
            GregorianCalendar cal = this.procesarRespuesta(info);
            cliente.close();
            String hora = horaLocal(cal);
            String fecha = fechaLocal(cal);
            
            String[] ret = new String[2];
            
            ret[0] = fecha;
            ret[1] = hora;
            
            return ret;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    private String horaLocal(GregorianCalendar cal){
        try{
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            
            String hora = sdf.format(cal.getTime());
            System.out.println(hora);
            
            return hora;
            
            
        }
        catch(Exception e ){
            Logger log = Logger.getLogger(Class.class.getName()); 
            log.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }
    
    private String fechaLocal(GregorianCalendar cal){
        try{            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            String fecha = sdf.format(cal.getTime());
            System.out.println(fecha);
            
            return fecha;
            
        }
        catch(Exception e ){
            Logger log = Logger.getLogger(Class.class.getName()); 
            log.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }
    
    //Procesamos el paquete NTP, y retornamos el objeto calendario,
    //para ser formateado de acuertdo a las necesidades del
    //usuario de la clase.
    private GregorianCalendar procesarRespuesta(TimeInfo info){
        
        NtpV3Packet mensaje = info.getMessage();
        int estrato = mensaje.getStratum();
        String refType;
        
        TimeStamp refNtpTime = mensaje.getReferenceTimeStamp();

        GregorianCalendar calendario = new GregorianCalendar();
        calendario.setTimeInMillis(refNtpTime.getTime());
               
        return calendario;
    } 
}
