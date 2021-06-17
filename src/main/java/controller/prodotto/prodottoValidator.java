package controller.prodotto;

import controller.http.RequestValidator;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

final class prodottoValidator {
    static RequestValidator validateForm(HttpServletRequest request){
        RequestValidator validator=new RequestValidator(request);
        validator.assertMatch("nome", Pattern.compile("^\\w{1,30}$"), "nome compreso tra 1 e 30 caratteri");
        validator.assertMatch("ingredienti", Pattern.compile("^\\w{2,100}$"), "ingredienti compreso tra 2 e 30 caratteri");
        validator.assertMatch("info", Pattern.compile("^\\w{1,50}$"), "info compreso tra 1 e 50 caratteri");
        validator.assertDouble("prezzo","prezzo deve essere un numero con la virgola");
        validator.assertMatch("sconto", Pattern.compile("^((100)|[0-9]?[0-9]?)$"),"sconto deve essere un intero tra 0 e 100");
        validator.assertMatch("tipologia", Pattern.compile("^\\w{2,20}$"), "tipologia compreso tra 2 e 20 catteri");
        return validator;
    }
}
