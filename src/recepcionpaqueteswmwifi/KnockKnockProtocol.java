/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recepcionpaqueteswmwifi;

/**
 *
 * @author Eduardo Murillo
 */
public class KnockKnockProtocol {
    
    public Mediciones procesarDatos(String datos, int tipo){
        try{
            //TODO procesamos y almacenamos los datos 

            //El frame esta divido en: cabecera (header) y carga (payload).
            //HEADER:
            //DELIMITADOR (3 bytes):  "<=>"
            //TIPO FRAME  (1 byte):  0x80 (\u0128)
            //Numero campos (1 byte): en binario (1: 01, 2: 02, ... => charaters
            //SEPARADOR (1 byte): '#'
            //SERIAL ID (10 byteS): ASCII
            //SERPARADPR (1 byte): '#'
            //WASPMOTE ID (0-16 bytes): nombre del waspmote
            //SEQUENCIA FRAME (1-3 byteS): 0 a 255 en ASCII

            //PAYLOAD:
            //Tres tipos:
            //DATOS SIMPLES:        #TC:23#
            //DATOS COMPLEJOS       #ACC:996;-67,-255#
            //DATOS ESPECIALES:     #DATE:14-11-21#

            //Solo usaremos frames ASCII
            
            //Para WM 3G y WIFI, el tamaño maximo del frame es MAX_FRAME = 150 por defecto.
            char[] datosArray = datos.toCharArray();

            //Procesar header
            int numeroCampos = 0;
            try{
                numeroCampos = 0;
                numeroCampos = (int)datosArray[4];          
            }
            catch(NumberFormatException e){
                numeroCampos = -1;
                e.printStackTrace();
            }

            //Obtenemos el waspmote id. Buscamos el segundo y tercer separador
            int indice = -1;
            int conteo  = 0;
            while(true){
                indice = datos.indexOf('#', indice+1);

                if(indice > 0){
                    conteo++;
                }

                if(conteo == 2){
                    break;
                }
            }

            //Obtenemos el wasmpote id
            int indiceTercerNumeral = datos.indexOf('#', indice + 1);
            String idWaspmote = datos.substring(indice+1, indiceTercerNumeral);

            //Luego procesamos los datos...
            Mediciones med = extraerDatosWaspmote(datos, tipo, idWaspmote);
            
            //Almacenar datos 
            //TODO.

            return med;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    
    //Funciones para extraer datos del payload del frame enviado
    //por el waspmote.    
    private Mediciones extraerDatosWaspmote(String info, int tipo, String idWaspmote){
        
        //Vemos donde empezamos
        String infoSinHeader;
        int numeroMediciones = 0;
        
        if(tipo == RecepcionPaquetesWMWifi.WASPMOTE_INUNDACIONES){
            numeroMediciones = RecepcionPaquetesWMWifi.NUMERO_MEDICIONES_WM_INUNDACIONES;
        }
        else if(tipo == RecepcionPaquetesWMWifi.WASPMOTE_CIUDAD){
            numeroMediciones = RecepcionPaquetesWMWifi.NUMERO_MEDICIONES_WM_CIUDAD;
        }
        else if(tipo == RecepcionPaquetesWMWifi.WASPMOTE_CAMARONERA){
            numeroMediciones = RecepcionPaquetesWMWifi.NUMERO_MEDICIONES_WM_CAMARONERA;
        }
        
        Mediciones mediciones = new Mediciones(numeroMediciones, idWaspmote);
        
        int indice = -1;
        int conteo  = 0;
        while(true){
            indice = info.indexOf('#', indice+1);
            
            if(indice > 0){
                conteo++;
            }
            
            if(conteo == 4){
                break;
            }
        }
        
        infoSinHeader = info.substring(indice);     //aqui ya tenemos el payload
        
        //Para el Waspmote de ciudad, tenemos cinco sensores
        int i = 0;
        int indiceNumeral = 0;
        int indiceNumeral2 = -1;
        indiceNumeral = infoSinHeader.indexOf('#') + 1;     //Queremos omitir el #
        for(i = 0; i < numeroMediciones; i++){
            //indiceNumeral = infoSinHeader.indexOf('#');
            indiceNumeral2 = infoSinHeader.indexOf('#', indiceNumeral+1);
            String medicion = infoSinHeader.substring(indiceNumeral, indiceNumeral2);
            System.out.println(medicion);            //para verificacion
            
            //Creamos la medicion
            mediciones.agregarMedicion(medicion);

            //Continuamos extrayendo los strings de los datos.          
            indiceNumeral = indiceNumeral2 + 1;
            
        }

        return mediciones;
    }
    
}
