package model.ordine;

import model.menu.Menu;
import model.menu.MenuExtractor;
import model.prodotto.Prodotto;
import model.prodotto.ProdottoExtractor;
import model.rider.Rider;
import model.rider.RiderExtractor;
import model.ristorante.Ristorante;
import model.ristorante.RistoranteDAO;
import model.ristorante.RistoranteExtractor;
import model.tipologia.Tipologia;
import model.utente.Utente;
import model.utente.UtenteExtractor;
import model.utility.ConPool;
import model.utility.Paginator;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class OrdineDAO {

    public Ordine doRetrieveById(int codiceOrdine) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT o.codiceOrdine, o.dataOrdine, o.totale, o.nota, o.oraPartenza, o.oraArrivo, o.metodoPagamento, o.giudizio, o.voto, o.consegnato, o.codRider_fk, rd.codiceRider, rd.email, rd.citta, rd.veicolo, u.codiceUtente, u.nome, u.cognome, u.email, u.saldo, u.provincia, u.citta, u.via, u.civico, u.interesse, u.amministratore, r.codiceRistorante, r.nome, r.provincia, r.citta, r.via, r.civico, r.info, r.spesaMinima, r.tassoConsegna, r.urlImmagine, r.rating, t.nome, t.descrizione FROM Ordine o LEFT JOIN Rider rd ON o.codRider_fk= rd.codiceRider INNER JOIN Utente u ON o.codUtente_fk=u.codiceUtente INNER JOIN Ristorante r ON o.codRis_fk=r.codiceRistorante INNER JOIN AppartenenzaRT art ON r.codiceRistorante=art.codRis_fk INNER JOIN Tipologia t ON art.nomeTip_fk=t.nome WHERE o.codiceOrdine=?");
            ps.setInt(1, codiceOrdine);
            ResultSet rs = ps.executeQuery();
            Ordine o = null;
            if (rs.next()) {
                o = OrdineExtractor.extract(rs);
                Rider rd = RiderExtractor.extract(rs);
                o.setRider(rd);
                Utente u = UtenteExtractor.extract(rs);
                o.setUtente(u);
                Ristorante r = RistoranteExtractor.extract(rs);
                do {
                    Tipologia t = new Tipologia();
                    t.setNome(rs.getString("t.nome"));
                    t.setDescrizione(rs.getString("t.descrizione"));
                    r.getTipologie().add(t);
                } while (rs.next());
                o.setRistorante(r);
            }

            if(o==null)
                return null;

            StringJoiner sj = new StringJoiner(",", "(", ")");
            sj.add(Integer.toString(codiceOrdine));
            Map<Integer, Map<Integer, OrdineItem>> menus = OrdineDAO.composizioneOM(conn, sj);
            Map<Integer, ArrayList<OrdineItem>> prodotti = OrdineDAO.composizioneOP(conn, sj);

            if(!prodotti.isEmpty())
                o.setOrdineItems(prodotti.get(codiceOrdine));

            if(!menus.isEmpty())
                o.getOrdineItems().addAll(menus.get(codiceOrdine).values());

            return o ;
        }
    }

    public ArrayList<Ordine> doRetrieveAll(Paginator paginator) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT o.codiceOrdine, o.dataOrdine, o.totale, o.nota, o.oraPartenza, o.oraArrivo, o.metodoPagamento, o.giudizio, o.voto, o.consegnato, o.codRider_fk, rd.codiceRider, rd.email, rd.citta, rd.veicolo, u.codiceUtente, u.nome, u.cognome, u.email, u.saldo, u.provincia, u.citta, u.via, u.civico, u.interesse, u.amministratore, r.codiceRistorante, r.nome, r.provincia, r.citta, r.via, r.civico, r.info, r.spesaMinima, r.tassoConsegna, r.urlImmagine, r.rating, t.nome, t.descrizione FROM Ordine o LEFT JOIN Rider rd ON o.codRider_fk= rd.codiceRider INNER JOIN Utente u ON o.codUtente_fk=u.codiceUtente INNER JOIN Ristorante r ON o.codRis_fk=r.codiceRistorante INNER JOIN AppartenenzaRT art ON r.codiceRistorante=art.codRis_fk INNER JOIN Tipologia t ON art.nomeTip_fk=t.nome LIMIT ?,?");
            ps.setInt(1, paginator.getOffset());
            ps.setInt(2, paginator.getLimit());
            ResultSet rs = ps.executeQuery();
            Map<Integer, Ordine> ordini = new LinkedHashMap<>();
            while (rs.next()) {
                int codiceOrdine = rs.getInt("o.codiceOrdine");
                if (!ordini.containsKey(codiceOrdine)) {
                    Ordine o = OrdineExtractor.extract(rs);
                    Utente u = UtenteExtractor.extract(rs);
                    o.setUtente(u);
                    Ristorante r = RistoranteExtractor.extract(rs);
                    System.out.println(r.getNome()+" "+ codiceOrdine);
                    o.setRistorante(r);
                    Rider rd = RiderExtractor.extract(rs);
                    o.setRider(rd);
                    ordini.put(codiceOrdine, o);
                }
                Tipologia t = new Tipologia();
                t.setNome(rs.getString("t.nome"));
                t.setDescrizione(rs.getString("t.descrizione"));
                ordini.get(codiceOrdine).getRistorante().getTipologie().add(t);
            }

            if(ordini.isEmpty())
                return null;

            StringJoiner sj=new StringJoiner(",", "(", ")");
            for(int key: ordini.keySet()){
                sj.add(Integer.toString(key));
            }

            Map<Integer, Map<Integer, OrdineItem>> menus = OrdineDAO.composizioneOM(conn, sj);
            Map<Integer, ArrayList<OrdineItem>> prodotti = OrdineDAO.composizioneOP(conn, sj);

            if(!prodotti.isEmpty()) {
                for (int key : prodotti.keySet())
                    ordini.get(key).setOrdineItems(prodotti.get(key));
            }

            if(!menus.isEmpty()){
                for(int key: menus.keySet())
                    ordini.get(key).getOrdineItems().addAll(menus.get(key).values());
            }

            return new ArrayList<>(ordini.values());
        }
    }

    public ArrayList<Ordine> doRetrieveByUtente(Utente u) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT o.codiceOrdine, o.dataOrdine, o.totale, o.nota, o.oraPartenza, o.oraArrivo, o.metodoPagamento, o.giudizio, o.voto, o.consegnato, o.codRider_fk, rd.codiceRider, rd.email, rd.citta, rd.veicolo, r.codiceRistorante, r.nome, r.provincia, r.citta, r.via, r.civico, r.info, r.spesaMinima, r.tassoConsegna, r.urlImmagine, r.rating, t.nome, t.descrizione FROM Ordine o LEFT JOIN Rider rd On o.codRider_fk=rd.codiceRider INNER JOIN Ristorante r ON o.codRis_fk=r.codiceRistorante INNER JOIN AppartenenzaRT art ON r.codiceRistorante=art.codRis_fk INNER JOIN Tipologia t ON art.nomeTip_fk=t.nome WHERE o.codUtente_fk=?");
            ps.setInt(1, u.getCodice());
            ResultSet rs = ps.executeQuery();
            Map<Integer, Ordine> ordini = new LinkedHashMap<>();
            while (rs.next()) {
                int codiceOrdine = rs.getInt("o.codiceOrdine");
                if (!ordini.containsKey(codiceOrdine)) {
                    Ordine o = OrdineExtractor.extract(rs);
                    o.setUtente(u);
                    Ristorante r = RistoranteExtractor.extract(rs);
                    o.setRistorante(r);
                    Rider rd = RiderExtractor.extract(rs);
                    o.setRider(rd);
                    ordini.put(codiceOrdine, o);
                }
                Tipologia t = new Tipologia();
                t.setNome(rs.getString("t.nome"));
                t.setDescrizione(rs.getString("t.descrizione"));
                ordini.get(codiceOrdine).getRistorante().getTipologie().add(t);
            }

            if(ordini.isEmpty())
                return null;

            StringJoiner sj=new StringJoiner(",", "(", ")");
            for(int key: ordini.keySet()){
                sj.add(Integer.toString(key));
            }

            Map<Integer, Map<Integer, OrdineItem>> menus = OrdineDAO.composizioneOM(conn, sj);
            Map<Integer, ArrayList<OrdineItem>> prodotti = OrdineDAO.composizioneOP(conn, sj);

            if(!prodotti.isEmpty()) {
                for (int key : prodotti.keySet())
                    ordini.get(key).setOrdineItems(prodotti.get(key));
            }

            if(!menus.isEmpty()){
                for(int key: menus.keySet())
                    ordini.get(key).getOrdineItems().addAll(menus.get(key).values());
            }

            return new ArrayList<>(ordini.values());
        }
    }

    public ArrayList<Ordine> doRetrieveByUtentePaginated(Utente u, Paginator paginator) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT o.codiceOrdine, o.dataOrdine, o.totale, o.nota, o.oraPartenza, o.oraArrivo, o.metodoPagamento, o.giudizio, o.voto, o.consegnato, o.codRider_fk, rd.codiceRider, rd.email, rd.citta, rd.veicolo, r.codiceRistorante, r.nome, r.provincia, r.citta, r.via, r.civico, r.info, r.spesaMinima, r.tassoConsegna, r.urlImmagine, r.rating, t.nome, t.descrizione FROM Ordine o LEFT JOIN Rider rd On o.codRider_fk=rd.codiceRider INNER JOIN Ristorante r ON o.codRis_fk=r.codiceRistorante INNER JOIN AppartenenzaRT art ON r.codiceRistorante=art.codRis_fk INNER JOIN Tipologia t ON art.nomeTip_fk=t.nome WHERE o.codUtente_fk=? LIMIT ?,?");
            ps.setInt(1, u.getCodice());
            ps.setInt(2, paginator.getOffset());
            ps.setInt(3, paginator.getLimit());
            ResultSet rs = ps.executeQuery();
            Map<Integer, Ordine> ordini = new LinkedHashMap<>();
            while (rs.next()) {
                int codiceOrdine = rs.getInt("o.codiceOrdine");
                if (!ordini.containsKey(codiceOrdine)) {
                    Ordine o = OrdineExtractor.extract(rs);
                    o.setUtente(u);
                    Ristorante r = RistoranteExtractor.extract(rs);
                    o.setRistorante(r);
                    Rider rd = RiderExtractor.extract(rs);
                    o.setRider(rd);
                    ordini.put(codiceOrdine, o);
                }
                Tipologia t = new Tipologia();
                t.setNome(rs.getString("t.nome"));
                t.setDescrizione(rs.getString("t.descrizione"));
                ordini.get(codiceOrdine).getRistorante().getTipologie().add(t);
            }

            if(ordini.isEmpty())
                return null;

            StringJoiner sj=new StringJoiner(",", "(", ")");
            for(int key: ordini.keySet()){
                sj.add(Integer.toString(key));
            }

            Map<Integer, Map<Integer, OrdineItem>> menus = OrdineDAO.composizioneOM(conn, sj);
            Map<Integer, ArrayList<OrdineItem>> prodotti = OrdineDAO.composizioneOP(conn, sj);

            if(!prodotti.isEmpty()) {
                for (int key : prodotti.keySet())
                    ordini.get(key).setOrdineItems(prodotti.get(key));
            }

            if(!menus.isEmpty()){
                for(int key: menus.keySet())
                    ordini.get(key).getOrdineItems().addAll(menus.get(key).values());
            }

            return new ArrayList<>(ordini.values());
        }
    }

    public ArrayList<Ordine> doRetrieveByRistorante(Ristorante r, Paginator paginator) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT o.codiceOrdine, o.dataOrdine, o.totale, o.nota, o.oraPartenza, o.oraArrivo, o.metodoPagamento, o.giudizio, o.voto, o.consegnato, o.codRider_fk, rd.codiceRider, rd.email, rd.citta, rd.veicolo, u.codiceUtente, u.nome, u.cognome, u.email, u.saldo, u.provincia, u.citta, u.via, u.civico, u.interesse, u.amministratore FROM Ordine o LEFT JOIN Rider rd On o.codRider_fk=rd.codiceRider INNER JOIN Utente u ON o.codUtente_fk=u.codiceUtente WHERE o.codRis_fk=? LIMIT ?,?");
            ps.setInt(1, r.getCodice());
            ps.setInt(2, paginator.getOffset());
            ps.setInt(3, paginator.getLimit());
            ResultSet rs = ps.executeQuery();
            ArrayList<Ordine> ordini = new ArrayList<>();
            while (rs.next()) {
                Ordine o = OrdineExtractor.extract(rs);
                o.setRistorante(r);
                Utente u = UtenteExtractor.extract(rs);
                o.setUtente(u);
                Rider rd = RiderExtractor.extract(rs);
                o.setRider(rd);

                ordini.add(o);
            }

            if(ordini.isEmpty())
                return null;

            StringJoiner sj=new StringJoiner(",", "(", ")");
            for(Ordine o: ordini){
                sj.add(Integer.toString(o.getCodice()));
            }

            Map<Integer, Map<Integer, OrdineItem>> menus = OrdineDAO.composizioneOM(conn, sj);
            Map<Integer, ArrayList<OrdineItem>> prodotti = OrdineDAO.composizioneOP(conn, sj);

            boolean trovato;
            if(!prodotti.isEmpty()){
                for(int key: prodotti.keySet()) {
                    trovato=false;
                    for (int i = 0; i < ordini.size() && !trovato; ++i)
                        if (ordini.get(i).getCodice() == key) {
                            ordini.get(i).setOrdineItems(prodotti.get(key));
                            trovato = true;
                    }
                }
            }

            if(!menus.isEmpty()) {
                for (int key : menus.keySet()) {
                    trovato = false;
                    for (int i = 0; i < ordini.size() && !trovato; ++i)
                        if (ordini.get(i).getCodice() == key) {
                            ordini.get(i).getOrdineItems().addAll(menus.get(key).values());
                            trovato = true;
                        }
                }
            }

            return ordini;
        }
    }

    public ArrayList<Ordine> doRetrieveByRider(Rider rd) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT o.codiceOrdine, o.dataOrdine, o.totale, o.nota, o.oraPartenza, o.oraArrivo, o.metodoPagamento, o.giudizio, o.voto, o.consegnato, o.codRider_fk, u.codiceUtente, u.nome, u.cognome, u.email, u.saldo, u.provincia, u.citta, u.via, u.civico, u.interesse, u.amministratore, r.codiceRistorante, r.nome, r.provincia, r.citta, r.via, r.civico, r.info, r.spesaMinima, r.tassoConsegna, r.urlImmagine, r.rating, t.nome, t.descrizione FROM Ordine o INNER JOIN Utente u ON o.codUtente_fk=u.codiceUtente LEFT JOIN Rider rd On o.codRider_fk=rd.codiceRider INNER JOIN Ristorante r ON o.codRis_fk=r.codiceRistorante INNER JOIN AppartenenzaRT art ON r.codiceRistorante=art.codRis_fk INNER JOIN Tipologia t ON art.nomeTip_fk=t.nome WHERE o.codRider_fk=?");
            ps.setInt(1, rd.getCodice());
            ResultSet rs = ps.executeQuery();
            Map<Integer, Ordine> ordini = new LinkedHashMap<>();
            while (rs.next()) {
                int codiceOrdine = rs.getInt("o.codiceOrdine");
                if (!ordini.containsKey(codiceOrdine)) {
                    Ordine o = OrdineExtractor.extract(rs);
                    o.setRider(rd);
                    Ristorante r = RistoranteExtractor.extract(rs);
                    o.setRistorante(r);
                    Utente u = UtenteExtractor.extract(rs);
                    o.setUtente(u);
                    ordini.put(codiceOrdine, o);
                }
                Tipologia t = new Tipologia();
                t.setNome(rs.getString("t.nome"));
                t.setDescrizione(rs.getString("t.descrizione"));
                ordini.get(codiceOrdine).getRistorante().getTipologie().add(t);
            }

            if(ordini.isEmpty())
                return null;

            StringJoiner sj=new StringJoiner(",", "(", ")");
            for(int key: ordini.keySet()){
                sj.add(Integer.toString(key));
            }

            Map<Integer, Map<Integer, OrdineItem>> menus = OrdineDAO.composizioneOM(conn, sj);
            Map<Integer, ArrayList<OrdineItem>> prodotti = OrdineDAO.composizioneOP(conn, sj);

            if(!prodotti.isEmpty()) {
                for (int key : prodotti.keySet())
                    ordini.get(key).setOrdineItems(prodotti.get(key));
            }

            if(!menus.isEmpty()){
                for(int key: menus.keySet())
                    ordini.get(key).getOrdineItems().addAll(menus.get(key).values());
            }

            return new ArrayList<>(ordini.values());
        }
    }


    public ArrayList<Ordine> doRetrieveByRiderPaginated(Rider rd, Paginator paginator) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT o.codiceOrdine, o.dataOrdine, o.totale, o.nota, o.oraPartenza, o.oraArrivo, o.metodoPagamento, o.giudizio, o.voto, o.consegnato, o.codRider_fk, u.codiceUtente, u.nome, u.cognome, u.email, u.saldo, u.provincia, u.citta, u.via, u.civico, u.interesse, u.amministratore, r.codiceRistorante, r.nome, r.provincia, r.citta, r.via, r.civico, r.info, r.spesaMinima, r.tassoConsegna, r.urlImmagine, r.rating, t.nome, t.descrizione FROM Ordine o INNER JOIN Utente u ON o.codUtente_fk=u.codiceUtente LEFT JOIN Rider rd On o.codRider_fk=rd.codiceRider INNER JOIN Ristorante r ON o.codRis_fk=r.codiceRistorante INNER JOIN AppartenenzaRT art ON r.codiceRistorante=art.codRis_fk INNER JOIN Tipologia t ON art.nomeTip_fk=t.nome WHERE o.codRider_fk=? LIMIT ?,?");
            ps.setInt(1, rd.getCodice());
            ps.setInt(2, paginator.getOffset());
            ps.setInt(3, paginator.getLimit());
            ResultSet rs = ps.executeQuery();
            Map<Integer, Ordine> ordini = new LinkedHashMap<>();
            while (rs.next()) {
                int codiceOrdine = rs.getInt("o.codiceOrdine");
                if (!ordini.containsKey(codiceOrdine)) {
                    Ordine o = OrdineExtractor.extract(rs);
                    o.setRider(rd);
                    Ristorante r = RistoranteExtractor.extract(rs);
                    o.setRistorante(r);
                    Utente u = UtenteExtractor.extract(rs);
                    o.setUtente(u);
                    ordini.put(codiceOrdine, o);
                }
                Tipologia t = new Tipologia();
                t.setNome(rs.getString("t.nome"));
                t.setDescrizione(rs.getString("t.descrizione"));
                ordini.get(codiceOrdine).getRistorante().getTipologie().add(t);
            }

            if(ordini.isEmpty())
                return null;

            StringJoiner sj=new StringJoiner(",", "(", ")");
            for(int key: ordini.keySet()){
                sj.add(Integer.toString(key));
            }

            Map<Integer, Map<Integer, OrdineItem>> menus = OrdineDAO.composizioneOM(conn, sj);
            Map<Integer, ArrayList<OrdineItem>> prodotti = OrdineDAO.composizioneOP(conn, sj);

            if(!prodotti.isEmpty()) {
                for (int key : prodotti.keySet())
                    ordini.get(key).setOrdineItems(prodotti.get(key));
            }

            if(!menus.isEmpty()){
                for(int key: menus.keySet())
                    ordini.get(key).getOrdineItems().addAll(menus.get(key).values());
            }

            return new ArrayList<>(ordini.values());
        }
    }

    public ArrayList<Ordine> doRetrieveByCitta(String citta, boolean consegnato, Paginator paginator) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT o.codiceOrdine, o.dataOrdine, o.totale, o.nota, o.oraPartenza, o.oraArrivo, o.metodoPagamento, o.giudizio, o.voto, o.consegnato, o.codRider_fk, rd.codiceRider, rd.email, rd.citta, rd.veicolo, u.codiceUtente, u.nome, u.cognome, u.email, u.saldo, u.provincia, u.citta, u.via, u.civico, u.interesse, u.amministratore, r.codiceRistorante, r.nome, r.provincia, r.citta, r.via, r.civico, r.info, r.spesaMinima, r.tassoConsegna, r.urlImmagine, r.rating, t.nome, t.descrizione FROM Ordine o LEFT JOIN Rider rd ON o.codRider_fk= rd.codiceRider INNER JOIN Utente u ON o.codUtente_fk=u.codiceUtente INNER JOIN Ristorante r ON o.codRis_fk=r.codiceRistorante INNER JOIN AppartenenzaRT art ON r.codiceRistorante=art.codRis_fk INNER JOIN Tipologia t ON art.nomeTip_fk=t.nome WHERE r.citta=? AND o.consegnato=? LIMIT ?,?  ");
            ps.setString(1, citta);
            ps.setBoolean(2, consegnato);
            ps.setInt(3, paginator.getOffset());
            ps.setInt(4, paginator.getLimit());
            ResultSet rs = ps.executeQuery();
            Map<Integer, Ordine> ordini = new LinkedHashMap<>();
            while (rs.next()) {
                int codiceOrdine = rs.getInt("o.codiceOrdine");
                if (!ordini.containsKey(codiceOrdine)) {
                    Ordine o = OrdineExtractor.extract(rs);
                    Utente u = UtenteExtractor.extract(rs);
                    o.setUtente(u);
                    Ristorante r = RistoranteExtractor.extract(rs);
                    o.setRistorante(r);
                    Rider rd = RiderExtractor.extract(rs);
                    o.setRider(rd);
                    ordini.put(codiceOrdine, o);
                }
                Tipologia t = new Tipologia();
                t.setNome(rs.getString("t.nome"));
                t.setDescrizione(rs.getString("t.descrizione"));
                ordini.get(codiceOrdine).getRistorante().getTipologie().add(t);
            }

            if(ordini.isEmpty())
                return null;

            StringJoiner sj=new StringJoiner(",", "(", ")");
            for(int key: ordini.keySet()){
                sj.add(Integer.toString(key));
            }

            Map<Integer, Map<Integer, OrdineItem>> menus = OrdineDAO.composizioneOM(conn, sj);
            Map<Integer, ArrayList<OrdineItem>> prodotti = OrdineDAO.composizioneOP(conn, sj);

            if(!prodotti.isEmpty()) {
                for (int key : prodotti.keySet())
                    ordini.get(key).setOrdineItems(prodotti.get(key));
            }

            if(!menus.isEmpty()){
                for(int key: menus.keySet())
                    ordini.get(key).getOrdineItems().addAll(menus.get(key).values());
            }

            return new ArrayList<>(ordini.values());
        }
    }

    //da vedere
    public ArrayList<Ordine> doRetrieveByData(LocalDate ld, Paginator paginator, int param) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            String sign = null;
            switch (param) {
                case 0:
                    sign = "=";
                    break;
                case 1:
                    sign = ">=";
                    break;
                case 2:
                    sign = "<=";
                    break;
                default:
                    sign = "=";
            }

            PreparedStatement ps = conn.prepareStatement("SELECT o.codiceOrdine, o.dataOrdine, o.totale, o.nota, o.oraPartenza, o.oraArrivo, o.metodoPagamento, o.giudizio, o.voto, o.consegnato, o.codRider_fk, rd.codiceRider, rd.email, rd.citta, rd.veicolo, u.codiceUtente, u.nome, u.cognome, u.email, u.saldo, u.provincia, u.citta, u.via, u.civico, u.interesse, u.amministratore, r.codiceRistorante, r.nome, r.provincia, r.citta, r.via, r.civico, r.info, r.spesaMinima, r.tassoConsegna, r.urlImmagine, r.rating, t.nome, t.descrizione FROM Ordine o LEFT JOIN Rider rd ON o.codRider_fk= rd.codiceRider INNER JOIN Utente u ON o.codUtente_fk=u.codiceUtente INNER JOIN Ristorante r ON o.codRis_fk=r.codiceRistorante INNER JOIN AppartenenzaRT art ON r.codiceRistorante=art.codRis_fk INNER JOIN Tipologia t ON art.nomeTip_fk=t.nome WHERE o.dataOrdine" + sign + "? LIMIT ?,?  ");
            ps.setDate(1, Date.valueOf(ld));
            ps.setInt(2, paginator.getOffset());
            ps.setInt(3, paginator.getLimit());
            ResultSet rs = ps.executeQuery();
            Map<Integer, Ordine> ordini = new LinkedHashMap<>();
            while (rs.next()) {
                int codiceOrdine = rs.getInt("o.codiceOrdine");
                if (!ordini.containsKey(codiceOrdine)) {
                    Ordine o = OrdineExtractor.extract(rs);
                    Utente u = UtenteExtractor.extract(rs);
                    o.setUtente(u);
                    Ristorante r = RistoranteExtractor.extract(rs);
                    o.setRistorante(r);
                    Rider rd = RiderExtractor.extract(rs);
                    o.setRider(rd);
                    ordini.put(codiceOrdine, o);
                }
                Tipologia t = new Tipologia();
                t.setNome(rs.getString("t.nome"));
                t.setDescrizione(rs.getString("t.descrizione"));
                ordini.get(codiceOrdine).getRistorante().getTipologie().add(t);
            }

            if(ordini.isEmpty())
                return null;

            StringJoiner sj=new StringJoiner(",", "(", ")");
            for(int key: ordini.keySet()){
                sj.add(Integer.toString(key));
            }

            Map<Integer, Map<Integer, OrdineItem>> menus = OrdineDAO.composizioneOM(conn, sj);
            Map<Integer, ArrayList<OrdineItem>> prodotti = OrdineDAO.composizioneOP(conn, sj);

            if(!prodotti.isEmpty()) {
                for (int key : prodotti.keySet())
                    ordini.get(key).setOrdineItems(prodotti.get(key));
            }

            if(!menus.isEmpty()){
                for(int key: menus.keySet())
                    ordini.get(key).getOrdineItems().addAll(menus.get(key).values());
            }

            return new ArrayList<>(ordini.values());
        }
    }

    public ArrayList<Ordine> doRetrieveBetweenData(LocalDate ldInizio, LocalDate ldFine, Paginator paginator) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("SELECT o.codiceOrdine, o.dataOrdine, o.totale, o.nota, o.oraPartenza, o.oraArrivo, o.metodoPagamento, o.giudizio, o.voto, o.consegnato, o.codRider_fk, rd.codiceRider, rd.email, rd.citta, rd.veicolo, u.codiceUtente, u.nome, u.cognome, u.email, u.saldo, u.provincia, u.citta, u.via, u.civico, u.interesse, u.amministratore, r.codiceRistorante, r.nome, r.provincia, r.citta, r.via, r.civico, r.info, r.spesaMinima, r.tassoConsegna, r.urlImmagine, r.rating, t.nome, t.descrizione FROM Ordine o LEFT JOIN Rider rd ON o.codRider_fk= rd.codiceRider INNER JOIN Utente u ON o.codUtente_fk=u.codiceUtente INNER JOIN Ristorante r ON o.codRis_fk=r.codiceRistorante INNER JOIN AppartenenzaRT art ON r.codiceRistorante=art.codRis_fk INNER JOIN Tipologia t ON art.nomeTip_fk=t.nome WHERE o.dataOrdine>=? AND o.dataOrdine<=? LIMIT ?,?  ");
            ps.setDate(1, Date.valueOf(ldInizio));
            ps.setDate(2, Date.valueOf(ldFine));
            ps.setInt(3, paginator.getOffset());
            ps.setInt(4, paginator.getLimit());
            ResultSet rs = ps.executeQuery();
            Map<Integer, Ordine> ordini = new LinkedHashMap<>();
            while (rs.next()) {
                int codiceOrdine = rs.getInt("o.codiceOrdine");
                if (!ordini.containsKey(codiceOrdine)) {
                    Ordine o = OrdineExtractor.extract(rs);
                    Utente u = UtenteExtractor.extract(rs);
                    o.setUtente(u);
                    Ristorante r = RistoranteExtractor.extract(rs);
                    o.setRistorante(r);
                    Rider rd = RiderExtractor.extract(rs);
                    o.setRider(rd);
                    ordini.put(codiceOrdine, o);
                }
                Tipologia t = new Tipologia();
                t.setNome(rs.getString("t.nome"));
                t.setDescrizione(rs.getString("t.descrizione"));
                ordini.get(codiceOrdine).getRistorante().getTipologie().add(t);
            }

            if(ordini.isEmpty())
                return null;

            StringJoiner sj=new StringJoiner(",", "(", ")");
            for(int key: ordini.keySet()){
                sj.add(Integer.toString(key));
            }

            Map<Integer, Map<Integer, OrdineItem>> menus = OrdineDAO.composizioneOM(conn, sj);
            Map<Integer, ArrayList<OrdineItem>> prodotti = OrdineDAO.composizioneOP(conn, sj);

            if(!prodotti.isEmpty()) {
                for (int key : prodotti.keySet())
                    ordini.get(key).setOrdineItems(prodotti.get(key));
            }

            if(!menus.isEmpty()){
                for(int key: menus.keySet())
                    ordini.get(key).getOrdineItems().addAll(menus.get(key).values());
            }

            return new ArrayList<>(ordini.values());
        }
    }

    public boolean doSave(Ordine o) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Ordine (dataOrdine, totale, nota, metodoPagamento, consegnato, codUtente_fk, codRis_fk) VALUES(?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setDate(1, Date.valueOf(o.getDataOrdine()));
            ps.setFloat(2, o.getTotale());
            ps.setString(3, o.getNota());
            ps.setString(4, o.getMetodoPagamento());
            ps.setBoolean(5, o.isConsegnato());
            ps.setInt(6, o.getUtente().getCodice());
            ps.setInt(7, o.getRistorante().getCodice());
            int rows = ps.executeUpdate();

            if (rows != 1) {
                conn.setAutoCommit(true);
                return false;
            }

            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            int id = rs.getInt(1);
            o.setCodice(id);

            int total = 0;
            for (OrdineItem oi : o.getOrdineItems()) {
                if (oi.getOff().getClass().getName().equals("model.prodotto.Prodotto"))
                    ps = conn.prepareStatement("INSERT INTO ComposizioneOP (codOrd_fk,codProd_fk,quantita) VALUES(?,?,?)");
                else
                    ps = conn.prepareStatement("INSERT INTO ComposizioneOM (codOrd_fk,codMenu_fk,quantita) VALUES(?,?,?)");
                ps.setInt(1, id);
                ps.setInt(2, oi.getOff().getCodice());
                ps.setInt(3, oi.getQuantita());
                total += ps.executeUpdate();
            }

            if (total == (rows * o.getOrdineItems().size())) {
                conn.commit();
                conn.setAutoCommit(true);
                return true;
            } else {
                conn.rollback();
                conn.setAutoCommit(true);
                return false;
            }
        }
    }


    public boolean doUpdate(Ordine o) throws SQLException {
        try(Connection conn=ConPool.getConnection()){
            PreparedStatement ps=conn.prepareStatement("UPDATE Ordine SET dataOrdine=?, totale=?, nota=?, oraPartenza=?, oraArrivo=?, metodoPagamento=?, consegnato=?, codUtente_fk=?, codRis_fk=? WHERE codiceOrdine=?");
            ps.setDate(1,Date.valueOf(o.getDataOrdine()));
            ps.setFloat(2, o.getTotale());
            ps.setString(3, o.getNota());
            ps.setTime(4, Time.valueOf(o.getOraPartenza()));
            ps.setTime(5, Time.valueOf(o.getOraArrivo()));
            ps.setString(6, o.getMetodoPagamento());
            ps.setBoolean(7, o.isConsegnato());
            ps.setInt(8, o.getUtente().getCodice());
            ps.setInt(9, o.getRistorante().getCodice());
            ps.setInt(10, o.getCodice());

            if(ps.executeUpdate()!=1)
                return false;
            else
                return true;
        }
    }

    public boolean updateVG(Ordine o) throws SQLException {
        try(Connection conn=ConPool.getConnection()){
            conn.setAutoCommit(false);
            PreparedStatement ps=conn.prepareStatement("UPDATE Ordine SET voto=?, giudizio=? WHERE codiceOrdine=?");
            ps.setInt(1,o.getVoto());
            ps.setString(2,o.getGiudizio());
            ps.setInt(3,o.getCodice());
            if(ps.executeUpdate()==1){
                ps=conn.prepareStatement("SELECT AVG(o.voto) as rating FROM Ordine o WHERE o.codRis_fk=?");
                ps.setInt(1,o.getRistorante().getCodice());
                ResultSet rs=ps.executeQuery();
                if(rs.next())
                {
                    o.getRistorante().setRating(rs.getInt("rating"));
                    RistoranteDAO service=new RistoranteDAO();
                    if(!service.doUpdate(o.getRistorante()))
                    {
                        conn.rollback();
                        conn.setAutoCommit(true);
                        return false;
                    }
                    conn.commit();
                    conn.setAutoCommit(true);
                    return true;
                }
                else{
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
            }
            else {
                conn.setAutoCommit(true);
                return false;
            }
        }
    }

    public boolean updateRider(int codiceOrdine, int codiceRider) throws SQLException {
        try(Connection conn=ConPool.getConnection()){
            PreparedStatement ps=conn.prepareStatement("UPDATE Ordine SET codRider_fk=? WHERE codiceOrdine=?");
            ps.setInt(1,codiceRider);
            ps.setInt(2,codiceOrdine);

            if(ps.executeUpdate()!=1)
                return false;
            else
                return true;
        }
    }

    public boolean doDelete(int codiceOrdine) throws SQLException{
        try(Connection conn=ConPool.getConnection()) {
            PreparedStatement ps=conn.prepareStatement("DELETE FROM ORDINE WHERE codiceOrdine=?");
            ps.setInt(1, codiceOrdine);
            if(ps.executeUpdate()!=1)
                return false;
            else
                return true;
        }
    }

    private static Map<Integer,Map<Integer, OrdineItem>> composizioneOM(Connection conn, StringJoiner sj) throws SQLException {
        PreparedStatement menu=conn.prepareStatement("SELECT m.codiceMenu, m.nome, m.prezzo, m.sconto, m.valido, com.codOrd_fk, com.quantita,  p.codiceProdotto, p.nome, p.ingredienti, p.info, p.prezzo, p.sconto, p.valido, p.urlImmagine, t.nome, t.descrizione FROM ComposizioneOM com INNER JOIN Menu m ON com.codMenu_fk=m.codiceMenu INNER JOIN AppartenenzaPM apm ON m.codiceMenu=apm.codMenu_fk INNER JOIN Prodotto p ON apm.codProd_fk=p.codiceProdotto INNER JOIN Tipologia t ON p.nomeTip_fk=t.nome WHERE com.codOrd_fk IN " + sj.toString());
        ResultSet rs=menu.executeQuery();
        Map<Integer, Map<Integer, OrdineItem>> ordineItems=new LinkedHashMap<>();

        while(rs.next()){
            int codiceOrdine=rs.getInt("com.codOrd_fk");
            if(!ordineItems.containsKey(codiceOrdine)){
                Map<Integer, OrdineItem> menus=new LinkedHashMap<>();
                ordineItems.put(codiceOrdine, menus);
            }
            int codiceMenu=rs.getInt("m.codiceMenu");
            if(!ordineItems.get(codiceOrdine).containsKey(codiceMenu)){
                Menu m=MenuExtractor.extract(rs);
                int quantita=rs.getInt("com.quantita");
                OrdineItem oi=new OrdineItem();
                oi.setOff(m);
                oi.setQuantita(quantita);
                ordineItems.get(codiceOrdine).put(codiceMenu, oi);
            }

            Prodotto p=ProdottoExtractor.extract(rs);
            Tipologia t=new Tipologia();
            t.setNome("t.nome");
            t.setDescrizione("t.descrizione");
            p.setTipologia(t);
            Menu m= (Menu) ordineItems.get(codiceOrdine).get(codiceMenu).getOff();
            m.getProdotti().add(p);
        }

        return ordineItems;
    }

    private static Map<Integer,ArrayList<OrdineItem>> composizioneOP(Connection conn, StringJoiner sj) throws SQLException {
        PreparedStatement prodotti=conn.prepareStatement("SELECT p.codiceProdotto, p.nome, p.ingredienti, p.info, p.prezzo, p.sconto, p.valido, p.urlImmagine, cop.codOrd_fk, cop.quantita, t.nome, t.descrizione FROM ComposizioneOP cop INNER JOIN Prodotto p ON cop.codProd_fk=p.codiceProdotto INNER JOIN Tipologia t ON p.nomeTip_fk=t.nome WHERE cop.codOrd_fk IN " + sj.toString());
        ResultSet rs=prodotti.executeQuery();
        Map<Integer, ArrayList<OrdineItem>> ordineItems=new LinkedHashMap<>();
        while(rs.next()){
            int codiceOrdine=rs.getInt("cop.codOrd_fk");
            if(!ordineItems.containsKey(codiceOrdine)){
                ArrayList<OrdineItem> ois = new ArrayList<>();
                ordineItems.put(codiceOrdine, ois);
            }
            Prodotto p=ProdottoExtractor.extract(rs);
            Tipologia t=new Tipologia();
            t.setNome("t.nome");
            t.setDescrizione("t.descrizione");
            p.setTipologia(t);
            int quantita=rs.getInt("cop.quantita");
            OrdineItem oi=new OrdineItem();
            oi.setOff(p);
            oi.setQuantita(quantita);
            ordineItems.get(codiceOrdine).add(oi);
        }

        return ordineItems;
    }
}
