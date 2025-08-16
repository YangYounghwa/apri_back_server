package apri.back_demo.exception;

public class PathNotFoundException extends RuntimeException{
    public PathNotFoundException(String message){
        super(message);
    }
}
