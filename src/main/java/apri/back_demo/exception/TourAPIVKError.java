package apri.back_demo.exception;

public class TourAPIVKError extends Exception {
    private Integer serviceErrorCode= null;

    public TourAPIVKError(String msg,Integer errorCode){
        super(msg);
        this.serviceErrorCode=errorCode;
    }

    public Integer getServiceErrorCode() {
        return serviceErrorCode;
    }
}
