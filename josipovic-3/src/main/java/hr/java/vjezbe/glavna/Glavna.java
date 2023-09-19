package hr.java.vjezbe.glavna;
import hr.java.vjezbe.entitet.*;
import hr.java.vjezbe.iznimke.NeispravanOdabirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;

//Labos - 3
public class Glavna {
    private static final Logger logger = LoggerFactory.getLogger(Glavna.class);
    private static final Integer brProf = 2, brPred = 2, brStud = 2, brIspit = 2, akadGod = 2022, brUstanova = 2;
    public static void main(String[] args) throws FileNotFoundException {
        logger.info("Aplikacija pokrenuta");

        //Scanner scanner = new Scanner(System.in);
        File file = new File("josipovic-3/src/main/java/hr/java/vjezbe/files/currentInput");
        Scanner scanner = new Scanner(file);

        Profesor[] profesori;
        Predmet[] predmeti;
        Student[] studenti;
        Ispit[] ispiti;

        int brObrUstanova = inputErrorHandle(scanner, "Unesite broj obrazovnih ustanova: ", 1, Integer.MAX_VALUE);

        ObrazovnaUstanova[] obrazovneUstanove = new ObrazovnaUstanova[brObrUstanova];

        for (int j = 0; j < obrazovneUstanove.length; j++) {
            System.out.println("Unesite podatke za " + (j + 1) + ". obrazovnu ustanovu:");

            profesori = unosProfesora(scanner);
            predmeti = unosPredmeta(scanner, profesori);
            studenti = unosStudenata(scanner);
            ispiti = unosIspita(scanner, predmeti, studenti);

            printOdlikasi(ispiti);

            int odabirUstanove = inputErrorHandle(scanner, "Odaberite obrazovnu ustanovu za navedene podatke koju želite unijeti (1 - Veleučilište Jave, 2 - Fakultet računarstva): ",
                    1 ,brObrUstanova);

            System.out.println("Unesite naziv obrazovne ustanove: ");
            String nazivUstanove = scanner.nextLine();

            if (odabirUstanove == 1) {
                obrazovneUstanove[j] = new VeleucilisteJave(nazivUstanove, predmeti, studenti, ispiti);
            }
            else if (odabirUstanove == 2) {
                obrazovneUstanove[j] = new FakultetRacunarstva(nazivUstanove, predmeti, studenti, ispiti);
            }
            //Handlao sam s iznimkama. Mislim da mogu izbaciti
            else {
                System.out.println("Neočekivana greška - Programer je kriv. ABORT");
                return;
            }


            //Konačna ocjena, najbolji student, rektorova nagrada - Staviti u funkciju kasnije
            for (Student s : obrazovneUstanove[j].getStudenti()) {
                Integer zavrsniPismeni = inputErrorHandle(scanner, "Unesite ocjenu završnog rada za studenta: " + s.getImePrezime() + ": ",1 ,5);
                Integer zavrsniObrana = inputErrorHandle(scanner, "Unesite ocjenu obrane završnog rada za studenta: " + s.getImePrezime() + ": ",1 ,5);

                if (obrazovneUstanove[j] instanceof Visokoskolska visokoskolska) {
                    //Ovo bi bilo idealno staviti u metodu koja handla ispite
                    Ispit[] studentoviIspiti = visokoskolska.filtrirajIspitePoStudentu(obrazovneUstanove[j].getIspiti(), s);
                    BigDecimal konacnaOcjena = visokoskolska.izracunajKonacnuOcjenuStudijaZaStudenta(studentoviIspiti, zavrsniPismeni, zavrsniObrana);
                    System.out.println("Konačna ocjena studija studenta " + s.getImePrezime() + " je " + konacnaOcjena);
                }
            }

            Student bestStudent = obrazovneUstanove[j].odrediNajuspjesnijegStudentaNaGodini(akadGod);
            System.out.println("Najbolji student " + akadGod + ". godine je " + bestStudent.getImePrezime() + " JMBAG: " + bestStudent.getJmbag());

            if (obrazovneUstanove[j] instanceof Diplomski diplomski) {
                Student rektorovaNagrada = diplomski.odrediStudentaZaRektorovuNagradu();
                System.out.println("Student koji je osvojio rektorovu nagradu je: " +
                        rektorovaNagrada.getImePrezime() + " JMBAG: " + rektorovaNagrada.getJmbag());
            }
            //Konačna ocjena, najbolji student, rektorova nagrada
        }
        scanner.close();
        logger.info("Aplikacija završila s radom");
    }

    private static int inputErrorHandle(Scanner scanner, String message) {
        int brObjekata = 0;
        Boolean badFormat;
        do {
            try {
                System.out.print(message);
                brObjekata = scanner.nextInt();
                badFormat = false;
            } catch (InputMismatchException e) {
                logger.error("Unesen string umjesto broja " + e);
                System.out.println("Neispravan unos!");
                badFormat = true;
            } finally {
                scanner.nextLine();
            }
        } while (badFormat);
        return brObjekata;
    }


    private static int inputErrorHandle(Scanner scanner, String message, int minVrijednost, int maxVrijednost){
        int redniBrObjekata = 0;
        Boolean badFormat;
        do {
            try {
                System.out.print(message);
                redniBrObjekata = scanner.nextInt();
                provjera(redniBrObjekata, minVrijednost, maxVrijednost);
                badFormat = false;
            } catch (InputMismatchException e) {
                logger.error("Unesen string umjesto broja " + e);
                System.out.println("Neispravan unos!");
                badFormat = true;
            }
            catch (NeispravanOdabirException e){
                logger.error(e.getMessage() + e);
                System.out.println("Unijeli ste nedozvoljen broj.");
                badFormat = true;
            }
            finally {
                scanner.nextLine();
            }
        } while (badFormat);
        return redniBrObjekata;
    }

    //Smislenije nazvati sve ove nove funkcije
    private static void provjera(int redniBrObjekata, int minVrijednost, int maxVrijednost){
        if (redniBrObjekata < minVrijednost || redniBrObjekata > maxVrijednost){
            throw new NeispravanOdabirException("Unesen nedozvoljen broj ");
        }
    }



    static Profesor[] unosProfesora(Scanner scanner) {
        Profesor[] profesori = new Profesor[brProf];
        for (int i = 0; i < profesori.length; i++) {
            System.out.println("Unesite " + (i + 1) + ". profesora: ");

            System.out.print("Unesite šifru profesora: ");
            String sifra = scanner.nextLine();

            System.out.print("Unesite ime profesora: ");
            String ime = scanner.nextLine();

            System.out.print("Unesite prezime profesora: ");
            String prezime = scanner.nextLine();

            System.out.print("Unesite titulu profesora: ");
            String titula = scanner.nextLine();

            profesori[i] = new Profesor.ProfesorBuilder().setSifra(sifra).setIme(ime).setPrezime(prezime).setTitula(titula).build();
        }
        return profesori;
    }

    static Predmet[] unosPredmeta(Scanner scanner, Profesor[] profesori) {
        Predmet[] predmeti = new Predmet[brPred];
        for (int i = 0; i < predmeti.length; i++) {
            System.out.println("Unesite " + (i + 1) + ". predmet: ");

            System.out.print("Unesite šifru predmeta: ");
            String sifra = scanner.nextLine();

            System.out.print("Unesite naziv predmeta: ");
            String naziv = scanner.nextLine();

            Integer brojEctsBodova = inputErrorHandle(scanner, "Unesite broj ECTS bodova za predmet '" + naziv + "': ", 0,35);

            System.out.println("Odaberite profesora:");
            for (int j = 0; j < profesori.length; j++)
                System.out.println((j + 1) + ". " + profesori[j].getImePrezime());

            int odabir = inputErrorHandle(scanner, "Odabir >> ", 1, profesori.length);
            Integer brStudNaPredmetu = inputErrorHandle(scanner,"Unesite broj studenata za predmetu '" + naziv + "': ", 1, Integer.MAX_VALUE);
            predmeti[i] = new Predmet(sifra, naziv, brojEctsBodova, profesori[odabir - 1]);
        }
        return predmeti;
    }

    static Student[] unosStudenata(Scanner scanner) {
        Student[] studenti = new Student[brStud];
        for (int i = 0; i < studenti.length; i++) {
            System.out.println("Unesite " + (i + 1) + ". studenta: ");

            System.out.print("Unesite ime studenta: ");
            String ime = scanner.nextLine();

            System.out.print("Unesite prezime studenta: ");
            String prezime = scanner.nextLine();

            System.out.print("Unesite datum rođenja studenta " + prezime + " " + ime + " u formatu (dd.MM.yyyy.): ");

            String stringDatumRodjenja = scanner.nextLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
            LocalDate datumRodjenja = LocalDate.parse(stringDatumRodjenja, formatter);

            System.out.print("Unesite JMBAG studenta: ");
            String jmbag = scanner.nextLine();

            studenti[i] = new Student(ime, prezime, jmbag, datumRodjenja);
        }
        return studenti;
    }

    static Ispit[] unosIspita(Scanner scanner, Predmet[] predmeti, Student[] studenti) {
        Ispit[] ispiti = new Ispit[brIspit];

        for (int i = 0; i < ispiti.length; i++) {
            System.out.println("Unesite " + (i + 1) + ". ispitni rok: ");

            System.out.println("Odaberite predmet: ");
            for (int j = 0; j < predmeti.length; j++)
                System.out.println((j + 1) + ". " + predmeti[j].getNaziv());

            //LAB-3-ZAD-3
            int odabirPredmeta = inputErrorHandle(scanner,"Odabir >> ", 1, predmeti.length);
            //LAB-3-ZAD-3

            System.out.println("Unesite naziv dvorane: ");
            String nazivDvorane = scanner.nextLine();

            System.out.println("Unesite zgradu dvorane: ");
            String zgradaDvorane = scanner.nextLine();

            Dvorana dvorana = new Dvorana(nazivDvorane, zgradaDvorane);

            System.out.println("Odaberite studenta: ");
            for (int j = 0; j < studenti.length; j++)
                System.out.println((j + 1) + ". " + studenti[j].getImePrezime());

            int odabirStudenta = inputErrorHandle(scanner, "Odabir >> ", 1, studenti.length);
            Integer ocjena = inputErrorHandle(scanner, "Unesite ocjenu na ispitu (1-5): ", 1, 5);

            System.out.print("Unesite datum i vrijeme ispita u formatu (dd.MM.yyyy.THH:mm): ");
            String stringDatumIVrijeme = scanner.nextLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.'T'HH:mm");
            LocalDateTime datumIVrijeme = LocalDateTime.parse(stringDatumIVrijeme, formatter);

            ispiti[i] = new Ispit(predmeti[odabirPredmeta - 1], studenti[odabirStudenta - 1], ocjena, datumIVrijeme, dvorana);

            //Objekte klase „Student“ koji su pristupili ispitima iz određeniH predmeta treba dodati u polje studenata za taj određeni predmet
            predmeti[odabirPredmeta - 1].addStudent(studenti[odabirStudenta - 1]);
        }
        return ispiti;
    }

    /**
     * Generic metoda koja printa sve elemente u polju.
     */
    public static <T> void printPolje(T[] items) {
        System.out.println("\n\n");
        for (T item : items) System.out.println(item.toString());
        System.out.println("\n\n");
    }

    /**
     * @param ispiti
     * Printa ime i prezime studenata koji imaju 5 iz ispita.
     */
    static void printOdlikasi(Ispit[] ispiti){
        for (Ispit i : ispiti){
            if (i.getOcjena() == 5){
                System.out.println("Student " + i.getStudent().getImePrezime()
                + " je ostvario ocjenu 'izvrstan' na predmetu '" + i.getPredmet().getNaziv() + "'");
            }
        }
    }

}