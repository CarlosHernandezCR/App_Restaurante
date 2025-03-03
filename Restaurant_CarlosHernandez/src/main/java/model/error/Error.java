package model.error;

import common.constants.ConstantsErrors;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Class that is going to divide the type of errors into their type
 * 1. FileReadingError
 * 2. FileWritingError
 * 3. EmptyList
 * 4. Error Adding
 * 5. Error Deleting
 * 6. Not Existing Object
 *
 */
@Getter
public class Error {
    private final int numError;
    private final String message;
    private final LocalDateTime date;

    public Error(String type,String message){
        date=LocalDateTime.now();
        this.message=message;
        if(type.compareTo(ConstantsErrors.FILEREADINGERROR)==0) {
            numError = 1;
        }else if(type.compareTo((ConstantsErrors.FILEWRITINGERROR))==0) {
            numError=2;
        }else if(type.compareTo(ConstantsErrors.EMPTYLISTERROR)==0){
            numError=3;
        }else if(type.compareTo(ConstantsErrors.ADDINGERROR)==0){
            numError=4;
        }else if(type.compareTo(ConstantsErrors.DELETINGERROR)==0){
            numError=5;
        }else numError=6;
    }
}
