package indi.etern.checkIn.entities.traffic;

public class Traffic {
    String dateString;
    private Traffic() {
        dateString = java.time.LocalDate.now().toString();
    }
//    @Override
    public String getStaticHash() {
        return dateString;
    }
}
