package com.fitconnect.bookingservice;
import feign.FeignException; import jakarta.persistence.EntityNotFoundException; import java.util.regex.*;
public class DownstreamUnavailableException extends RuntimeException {public DownstreamUnavailableException(String message){super(message);}
 private static final Pattern MESSAGE=Pattern.compile("\"message\"\\s*:\\s*\"([^\"]*)\"");
 // Une réponse métier (404/409) du service appelé n'est pas une panne : on la propage telle quelle au lieu de répondre 503.
 public static RuntimeException from(String service,Throwable cause){
  for(Throwable t=cause;t!=null;t=t.getCause()) if(t instanceof FeignException f){
   Matcher m=MESSAGE.matcher(f.contentUTF8());String message=m.find()?m.group(1):service+" a répondu "+f.status();
   if(f.status()==404)return new EntityNotFoundException(message);
   if(f.status()==409)return new IllegalStateException(message);
   if(f.status()==400)return new IllegalArgumentException(message);
   break;
  }
  return new DownstreamUnavailableException(service+" indisponible");
 }
}
