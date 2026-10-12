package clinicabalbarrey;

import com.toedter.calendar.IDateEvaluator;
import java.awt.Color;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EvaluadorFeriados implements IDateEvaluator {
    private List<Date> feriados;
    private List<String> diasQueAtiende; // Lista de días: "Lunes", "Martes", etc.

    public EvaluadorFeriados(List<Date> feriados, List<String> diasQueAtiende) {
        this.feriados = feriados;
        this.diasQueAtiende = diasQueAtiende;
    }

    @Override
    public boolean isInvalid(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // 1. Bloqueamos Domingos
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) return true;

        // 2. Bloqueamos Feriados
        for (Date f : feriados) {
            Calendar calFeriado = Calendar.getInstance();
            calFeriado.setTime(f);
            if (cal.get(Calendar.YEAR) == calFeriado.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == calFeriado.get(Calendar.DAY_OF_YEAR)) {
                return true;
            }
        }

        // 3. BLOQUEO DINÁMICO: Si el médico no trabaja este día de la semana
        if (diasQueAtiende != null && !diasQueAtiende.isEmpty()) {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            String[] nombresJava = {"", "Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado"};
            String diaActual = nombresJava[dayOfWeek];

            // Si el día actual NO está en la lista de días que atiende, lo bloqueamos
            if (!diasQueAtiende.contains(diaActual)) {
                return true;
            }
        }

        return false;
    }

    @Override public Color getInvalidForegroundColor() { return Color.WHITE; }
    @Override public Color getInvalidBackroundColor() { return new Color(211, 47, 47); }
    @Override public String getInvalidTooltip() { return "Médico no disponible / Feriado"; }
    @Override public boolean isSpecial(Date date) { return false; }
    @Override public Color getSpecialForegroundColor() { return null; }
    @Override public Color getSpecialBackroundColor() { return null; }
    @Override public String getSpecialTooltip() { return null; }
}