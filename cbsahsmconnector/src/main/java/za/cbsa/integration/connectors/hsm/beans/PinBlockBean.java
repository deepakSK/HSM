package za.cbsa.integration.connectors.hsm.beans;

/**
 *
 * @author deepak
 */
public class PinBlockBean {
    
   
    private String pin;
    private String processedBlock;

    public String getProcessedBlock() {
        return processedBlock;
    }

    public void setProcessedBlock(String processedBlock) {
        this.processedBlock = processedBlock;
    }

    public PinBlockBean(String pin) {
   
        this.pin = pin;
    }
    
    

   
    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
    
}
