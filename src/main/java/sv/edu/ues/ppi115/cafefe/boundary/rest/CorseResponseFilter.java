package sv.edu.ues.ppi115.cafefe.boundary.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List; 

@Provider
public class CorseResponseFilter implements ContainerResponseFilter {

    public static final String METODOS_PERMITIDOS="GET, POST, PUT, DELETE, OPTIONS, HEAD";
    public static final int MAXIMO_CACHE=30*60*60;
    public static final String CABECERAS_PERMITIDAS="origin,accept,content-type";
    public static final String CABECERAS_EXPUESTAS="location,info,Total-Records";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Headers", getRequestedAllowedHeaders(requestContext));
        headers.add("Access-Control-Expose-Headers", getRequestedExposedHeaders(requestContext));
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Methods", METODOS_PERMITIDOS);
        headers.add("Access-Control-Max-Age", MAXIMO_CACHE);
        headers.add("x-responded-by", "cors-response-filter");
    }

    String getRequestedAllowedHeaders(ContainerRequestContext responseContext) {
        List<String> headers = responseContext.getHeaders().get("Access-Control-Allow-Headers");
        return crearCabeceras(headers, CABECERAS_PERMITIDAS);
    }

    String getRequestedExposedHeaders(ContainerRequestContext requestContext) {
        List<String> headers = requestContext.getHeaders().get("Access-Control-Expose-Headers");
        return crearCabeceras(headers, CABECERAS_EXPUESTAS);
    }

    String crearCabeceras(List<String> cabeceras, String cabecerasPorDefecto){
        if (cabeceras==null || cabeceras.isEmpty()){
            return cabecerasPorDefecto;
        }
        List<String> salida = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Object cabecera : cabeceras){
            sb.append(cabecera);
            sb.append(";");
        }
        sb.append(cabecerasPorDefecto);
        return sb.toString();
    }
}