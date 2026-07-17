package be.hogent.baristajob2026.advice;

import be.hogent.baristajob2026.controller.*;
import be.hogent.baristajob2026.exception.BaristaNotFoundException;
import be.hogent.baristajob2026.exception.OpleidingNotFoundException;
import be.hogent.baristajob2026.exception.ShiftNotFoundException;
import be.hogent.baristajob2026.exception.VestigingNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// enkel voor de MVC (@Controller) klassen; ApiController heeft zijn eigen ApiExceptionAdvice
// (JSON i.p.v. redirect/view), dus we sluiten die hier bewust uit om overlap te vermijden
@ControllerAdvice(assignableTypes = {
        BaristaController.class,
        VestigingController.class,
        OpleidingController.class,
        ShiftController.class,
        OverviewController.class
})
public class GlobalExceptionAdvice {

    @ExceptionHandler(BaristaNotFoundException.class)
    public String handleBaristaNotFound() {
        return "redirect:/overview";
    }

    @ExceptionHandler(VestigingNotFoundException.class)
    public String handleVestigingNotFound() {
        return "redirect:/overview";
    }

    @ExceptionHandler(ShiftNotFoundException.class)
    public String handleShiftNotFound() {
        return "redirect:/overview";
    }

    @ExceptionHandler(OpleidingNotFoundException.class)
    public String handleOpleidingNotFound() {
        return "redirect:/overview";
    }

    // onjuiste URL's opvangen: bv. /vestiging/abc waar een Long verwacht wordt
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleInvalidIdFormat() {
        return "error/404";
    }
}
