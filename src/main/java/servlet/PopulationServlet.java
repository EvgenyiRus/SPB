package servlet;

import model.Gender;
import model.Person;
import org.slf4j.Logger;
import service.PersonService;
import service.impl.IPersonService;
import util.DatabaseUtil;
import util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

@WebServlet(
        name = "PopulationServlet",
        urlPatterns = "api/1.0/population",
        loadOnStartup = 1
)
public class PopulationServlet extends HttpServlet {
    private static final Logger logger = getLogger(PopulationServlet.class);
    private PersonService personService;
    private final Map<String, Long> requestCounters = new ConcurrentHashMap<>();


    @Override
    public void init() throws ServletException {
        super.init();
        try {
            this.personService = new IPersonService(DatabaseUtil.getConnection(), 99, 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestId = generateRequestId(request.getRemoteAddr());
        logger.info("Processing request {} from {}", requestId, request.getRemoteAddr());
        try {
            Collection data = JsonUtil.parseJsonRequest(request, Collection.class);

            // Обход и валидация первоначальных данных
            Iterator<Map<String, String>> iterator = data.iterator();
            Collection<Person> persons = new ArrayList<>();
            while (iterator.hasNext()) {
                Person person = createPersonFromData(iterator.next());
                if (person.isValidatePerson(person)) {
                    persons.add(person);
                }
            }
            personService.processPersons(persons);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            response.getWriter().write("{\"status\":\"accepted\", \"requestId\":\"" + requestId + "\"}");
        } catch (Exception e) {
            logger.error("Error processing request {}: {}", requestId, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\", \"requestId\":\"" + requestId + "\"}");
        }
    }

    private Person createPersonFromData(Map<String, String> data) {
        Person person = new Person();
        person.setGender(Gender.valueOf(data.get("gender").toUpperCase()));
        person.setBirthDate(LocalDate.parse(data.get("birthDate"), DateTimeFormatter.ISO_DATE));
        person.setRegion(Integer.parseInt(data.get("region")));
        String incomeStr = data.get("income");
        if (incomeStr != null && !incomeStr.isEmpty()) {
            person.setIncome(BigDecimal.valueOf(Long.parseLong(incomeStr)));
        }
        return person;
    }

    private String generateRequestId(String clientIp) {
        Long counter = requestCounters.compute(clientIp, (k, v) -> v == null ? 1L : v + 1);
        return clientIp + "-" + counter + "-" + System.currentTimeMillis();
    }
}
