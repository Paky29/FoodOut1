package model.menu;

import model.prodotto.Prodotto;
import model.prodotto.ProdottoDAO;
import model.utility.ConPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MenuDAO {
    public MenuDAO() {}

    public Menu doRetrieveById(int codiceMenu) throws SQLException{
        try(Connection conn=ConPool.getConnection()){
            PreparedStatement ps=conn.prepareStatement("SELECT codiceMenu, m.nome, m.prezzo, m.sconto, m.valido, p.codiceProdotto, p.nome FROM Menu m INNER JOIN AppartenenzaPM apm ON m.codiceMenu=apm.codMenu_fk INNER JOIN Prodotto p ON apm.codProd_fk=p.codiceProdotto WHERE apm.codMenu_fk=? ");
            ps.setInt(1,codiceMenu);
            ResultSet rs=ps.executeQuery();
            Menu m=null;
            if(rs.next()){
                m=MenuExtractor.extract(rs);
                do {
                    Prodotto p=new Prodotto();
                    p.setCodice(rs.getInt("p.codiceProdotto"));
                    p.setNome(rs.getString("p.nome"));
                }while(rs.next());
            }
            return m;
        }
    }

    //da utilizzare per suggerimenti di menu
    public ArrayList<Menu> doRetrieveByProdotto(int codiceProdotto) throws SQLException{
        try(Connection conn=ConPool.getConnection()){
            PreparedStatement ps=conn.prepareStatement("SELECT codiceMenu, nome, prezzo, sconto, valido FROM Menu m INNER JOIN AppartenenzaPM apm ON m.codiceMenu=apm.codMenu_fk WHERE apm.codProd_fk=?");
            ps.setInt(1,codiceProdotto);
            ResultSet rs=ps.executeQuery();
            ArrayList<Menu> menus=new ArrayList<>();
            while(rs.next()){
                Menu m=MenuExtractor.extract(rs);
                menus.add(m);
            }
            if(menus.isEmpty())
                return null;
            else
                return menus;
        }
    }

    public ArrayList<Menu> doRetrieveByRistorante(int codiceRistorante) throws SQLException{
        try(Connection conn=ConPool.getConnection()){
            PreparedStatement ps=conn.prepareStatement("SELECT codiceMenu, m.nome, m.prezzo, m.sconto, m.valido, p.codiceProdotto, p.nome FROM Menu m INNER JOIN AppartenenzaPM apm ON m.codiceMenu=apm.codMenu_fk INNER JOIN Prodotto p ON apm.codProd_fk=p.codiceProdotto INNER JOIN Ristorante r ON p.codRis_fk=r.codiceRistorante WHERE r.codiceRistorante=?");
            ps.setInt(1, codiceRistorante);
            ResultSet rs=ps.executeQuery();
            Map<Integer, Menu> menus=new LinkedHashMap<>();
            while(rs.next()){
                int menuid=rs.getInt(1);
                if(!menus.containsKey(menuid)){
                    Menu m=MenuExtractor.extract(rs);
                    menus.put(menuid,m);
                }
                Prodotto p=new Prodotto();
                p.setNome(rs.getString("p.nome"));
                p.setCodice(rs.getInt("p.codiceProdotto"));
                menus.get(menuid).getProdotti().add(p);
            }
            if(menus.isEmpty())
                return null;
            else
                return new ArrayList<>(menus.values());
        }
    }

    public boolean doSave(Menu m) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement ps=conn.prepareStatement("INSERT INTO Menu (nome, prezzo, sconto, valido) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,m.getNome());
            ps.setFloat(2,m.getPrezzo());
            ps.setInt(3,m.getSconto());
            ps.setBoolean(4,m.isValido());
            int rows=ps.executeUpdate();

            if(rows!=1)
            {
                conn.setAutoCommit(true);
                return false;
            }

            ResultSet rs=ps.getGeneratedKeys();
            rs.next();
            int id=rs.getInt(1);
            m.setCodice(id);

            int total=0;
            for(Prodotto p:m.getProdotti()){
                ps=conn.prepareStatement("INSERT INTO AppartenenzaPM (conMenu_fk, codProd_fk) VALUES (?,?) ");
                ps.setInt(1,id);
                ps.setInt(2,p.getCodice());
                total+=ps.executeUpdate();
            }

            if(total==(rows*m.getProdotti().size()))
            {
                conn.commit();
                conn.setAutoCommit(true);
                return true;
            }
            else
            {
                conn.rollback();
                conn.setAutoCommit(true);
                return false;
            }
        }
    }

    public boolean doUpdate(Menu m) throws SQLException {
        try(Connection conn=ConPool.getConnection()){
            PreparedStatement ps=conn.prepareStatement("UPDATE Menu SET nome=?, prezzo=?, sconto=? WHERE codiceMenu=?");
            ps.setString(1,m.getNome());
            ps.setFloat(2,m.getPrezzo());
            ps.setInt(3,m.getSconto());
            ps.setInt(4,m.getCodice());
            if(ps.executeUpdate()!=1)
                return false;
            else
                return true;
        }
    }

    public boolean updateValidita(int codiceMenu, boolean valido) throws SQLException {
        try (Connection conn = ConPool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE Menu SET valido=? WHERE codiceMenu=?");
            ps.setBoolean(1, valido);
            ps.setInt(2, codiceMenu);
            if (ps.executeUpdate() != 1)
                return false;
            else
                return true;
        }
    }
}