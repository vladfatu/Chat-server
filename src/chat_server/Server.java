/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package chat_server;

/**
 *
 * @author Vlad
 */
import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
class Input extends Thread{
    DataInputStream stream;
    Statement stmt1;
    String mesaj;
    Client c1;              //clientul curent
    Client c2[];            //am nevoie de toate obiectele de tip Client
    public Input(Client c[],int i,Statement stmt) throws IOException {
        c1=c[i];
        c2=c;
        stmt1=stmt;
        int a=1;
        stream = new DataInputStream(c1.s.getInputStream());
        String stare="";
        //in acest while iau porecla clientului, se repeta pana cand serverul primeste o porecla unica
        while(a==1)
        {
        try {
                mesaj = stream.readUTF();
                StringTokenizer st = new StringTokenizer(mesaj);
                //st.nextToken();
                stare=st.nextToken().toString();                
                if (st.hasMoreTokens()) c1.nume=st.nextToken().toString();                
                if (st.hasMoreTokens()) c1.parola=st.nextToken().toString();
                else c1.parola="";
                //c1.nume=mesaj;
            }
            catch (IOException e) {
                System.out.println("[eroare]: " + stare);
            }
        if (stare.equals("/quit")) {c1.ok=false;c1.s.close();a=0; break;}
        else
        {
        if (unic(stmt,c1.nume)&&isRightFormat(c1.nume)&&stare.equals("reg")) {cont_nou(stmt,c1.nume,c1.parola);a=0;afiseaza(c1.s,"bine");}
        else if ((!exista(stmt,c1.nume,c1.parola))&&isRightFormat(c1.nume)&&stare.equals("log")) {a=0; afiseaza(c1.s,"bine");afiseaza(c1.s,"/prieteni "+prieteni(stmt1,c1.nume));stare(stmt1,c1.nume," @");}
        else afiseaza(c1.s,"Eroare");
        }
        }
        if (!mesaj.equals("/quit")) {start();}
    }
    public synchronized void inserare(Statement stmt,String s,String nume)
    {
        String my_stmt =
      "INSERT INTO "+s+" (Porecla ) VALUES('" + nume + "');";
        try {
            int i = stmt.executeUpdate(my_stmt);
            //System.out.println(i);
        } catch (SQLException ex) {
            System.out.println("sdfsdf");
        }
    }
    //Functia va
    public synchronized void cont_nou(Statement stmt,String nume,String parola)
    {
        String my_stmt =
      "INSERT INTO INFO (Porecla , PAROLA) VALUES('" + nume + "','"
 + parola + "');";
        try {
            int i = stmt.executeUpdate(my_stmt);
            //System.out.println(i);
        } catch (SQLException ex) {
            System.out.println("sdfsdf");
        }
        my_stmt =
      "create table "+nume+" (" +

							"Porecla VARCHAR(20) )";
        try {
            int i = stmt.executeUpdate(my_stmt);
            //System.out.println(i);
        } catch (SQLException ex) {
            System.out.println("sdfsdf");
        }
    }
    //Verific daca stringul "s" contine doar cifre, litere si caracterul "-"
    public Boolean isRightFormat(String s)
    {
        if (s.length()>16) return false;
        char chr[] = null;
	if(s != null)
		chr = s.toCharArray();
        for(int i=0; i<chr.length; i++)
	{
		if(!((chr[i] >= '0' && chr[i] <= '9') || (chr[i] >= 'A' && chr[i] <= 'Z') || (chr[i] >= 'a' && chr[i] <= 'z') || (chr[i]=='-')))
                return false;
        }
        return true;
    }

    //se afiseaza sirul de caractere "str" la clientul cu socket "s"
    public synchronized void afiseaza(Socket s,String str) throws IOException
    {
        DataOutputStream out=new DataOutputStream(s.getOutputStream());
        out.writeUTF(str);
    }
    public synchronized void transfera(DataInputStream stream,DataOutputStream out) throws IOException
    {
        int k;
                    while( ((k=stream.read()) != -1)&& k!=0) out.write(k);
                    out.write(0);
    }

    //se afiseaza numele tuturor clientilor inclusiv clientului curent
    public void list(Client c[],Socket s) throws IOException
    {
        DataOutputStream out=new DataOutputStream(s.getOutputStream());
        for (int i=1;i<V.n;i++)
        {
           if (c[i].ok) out.writeUTF(c[i].nume);
        }
    }

    //returneaza socketul cu numele s daca exista, altfel returneaza null
    public Socket cauta(Client c[],String s)
    {
        for (int i=1;i<V.n;i++)
            if (c[i].nume.equals(s) && c[i].ok) return c[i].s;
        return null;
    }

    //trimite mesajul "str" catre toti clientii in afara de cel cu socketul "s"
    public void bcast(Client c[],Socket s,String str) throws IOException
    {
        for (int i=1;i<V.n;i++) if (c[i].s!=s && c[i].ok) afiseaza(c[i].s,str);
    }

    //trimite mesajul "str" catre clientul cu porecla "nume"
    public void msg(Client c[],Socket s, String nume, String str) throws IOException
    {
        Socket s1;
        if ((s1=cauta(c,nume))!=null)
        {
            //if (s1==s) afiseaza(s,"Nu iti poti trimite mesaj tie insuti");
            //else afiseaza(s1,str);
            afiseaza(s1,str);
        }
        else 
        {
            //afiseaza(s, "Nu exista nici un client cu numele " + nume);
            System.out.println("Nu exista nici un client cu numele " + nume);
        }
    }

    //schimba porecla clientului "c" su stringul "s"
    public void nick(Client c, String s)
    {
        c.nume=s;
    }

    //returneaza true daca numele e unic, altfel returneaza false
    public Boolean unic(Statement stmt , String s)
    {
            String my_query = "SELECT * FROM INFO WHERE Porecla='"
 			 + s +  "'";
    ResultSet rs=null;
        try {
            rs = stmt.executeQuery(my_query);
        } catch (SQLException ex) {
            System.out.println("nu pot intreba baza de date");
        }
        if (rs==null) return true;
        else
        try {
            if (rs.next()) {
                return false;
            }
        } catch (SQLException ex) {
            System.out.println("nu pot accesa rezultatul");
        }

        /*try {
            if (rs.getString("Porecla").equals(s)) 
                return false;
        } catch (SQLException ex) {
            System.out.println("nu pot accesa rezultatul");
        }*/
    return true;
    }
    public Boolean exista(Statement stmt , String s , String s1)
    {
            String my_query = "SELECT * FROM INFO WHERE Porecla='"
 			 + s + "' AND PAROLA='" + s1 + "'";
    ResultSet rs=null;
        try {
            rs = stmt.executeQuery(my_query);
        } catch (SQLException ex) {
            System.out.println("nu pot intreba baza de date");
        }
        if (rs==null) return true;
        else
        try {
            if (rs.next()) {
                return false;
            }
        } catch (SQLException ex) {
            System.out.println("nu pot accesa rezultatul");
        }

        /*try {
            if (rs.getString("Porecla").equals(s))
                return false;
        } catch (SQLException ex) {
            System.out.println("nu pot accesa rezultatul");
        }*/
    return true;
    }
    public String prieteni(Statement stmt , String s)
    {
        String s1="",s2="";
        String my_query = "SELECT * FROM " +s+ "'";
        ResultSet rs=null;
        Socket st=null;
        try {
            rs = stmt.executeQuery(my_query);
        } catch (SQLException ex) {
            System.out.println("nu pot intreba baza de date");
        }
        if (rs==null) return s1;
        else
        try {
            while (rs.next())
            {
            s2=rs.getString("Porecla");
            s1+=s2;
            if ((st=cauta(c2,s2))!=null) s1+=" @";
            else s1+=" *";
            s1+=" ";
            }
        } catch (SQLException ex) {
            System.out.println("nu pot accesa rezultatul");
        }
        //System.out.println(s1);
        return s1;
    }
    public void stare (Statement stmt , String s,String str)
    {
        Socket stest=null;
        StringTokenizer st = new StringTokenizer(prieteni(stmt,s));
        while(st.hasMoreTokens())
        {
           if ((stest=cauta(c2,st.nextToken()))!=null)
            try {
                afiseaza(stest, "/stare "+s+str);
            } catch (IOException ex) {
                Logger.getLogger(Input.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public Boolean exista(Statement stmt , String s)
    {
            String my_query = "SELECT * FROM INFO WHERE Porecla='"
 			 + s + "'";
    ResultSet rs=null;
        try {
            rs = stmt.executeQuery(my_query);
        } catch (SQLException ex) {
            System.out.println("nu pot intreba baza de date");
        }
        if (rs==null) return false;
        else
        try {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            System.out.println("nu pot accesa rezultatul");
        }
    return false;
    }

    public void run() {
        while (c1.ok) {
            try {
                String s3="",s1="",s2="";;
                Socket stest=null;
                mesaj = stream.readUTF();
                StringTokenizer st = new StringTokenizer(mesaj);
                if (st.hasMoreTokens()) s3=st.nextToken();
                if (mesaj.equals("/quit")) {c1.ok=false;c1.s.close();stare(stmt1,c1.nume," *");}
                else if (mesaj.equals("/list")) list(c2,c1.s);
                else if (s3.equals("/msg"))
                {
                //separ stringul in cuvinte pt a alege parametrii metodei "msg"
                    
                    s1=st.nextToken();
                    while(st.hasMoreTokens())s2+=st.nextToken()+" ";
                    msg(c2,c1.s,s1,"["+c1.nume+"]: "+s2);
                }
                else if (s3.equals("/transfer"))
                {
                //separ stringul in cuvinte pt a alege parametrii metodei "msg"
                    String fisier="";
                    s1=st.nextToken();
                    while(st.hasMoreTokens())fisier+=st.nextToken()+" ";
                    int p = fisier.lastIndexOf("\\");
                    fisier = fisier.substring(p+1,fisier.length());
                    msg(c2,c1.s,s1,"["+c1.nume+"]: "+"se trimite fisierul "+fisier);
                    Socket soc;
                    DataOutputStream out=null;
                    if ((soc=cauta(c2,s1))!=null)
                    {
                        out=new DataOutputStream(soc.getOutputStream());
                    }
                    else afiseaza(c1.s,"Nu exista nici un client cu numele "+s1);
                    msg(c2,c1.s,s1,"/transfer "+fisier);
                    System.out.println("Se primeste fisierul : "
                         + fisier);
                    FileOutputStream fos = new FileOutputStream(fisier);
                    transfera(stream,out);
                    /*int k;
                    while( ((k=stream.read()) != -1)&& k!=0) out.write(k);
                    out.write(0);*/
                    fos.close(); System.out.println("Fisier primit !");
                }
                else if (s3.equals("/cauta"))
                {
                    if (st.hasMoreTokens()) s1=st.nextToken();
                    if (exista(stmt1,s1)) 
                    {
                        if ((stest=cauta(c2,s1))!=null) afiseaza(c1.s,"/adauga "+s1+" @");
                        else afiseaza(c1.s,"/adauga "+s1+" *");
                        inserare(stmt1,c1.nume,s1);
                    }
                    else afiseaza(c1.s,"/adauga prost");
                }
                else if (s3.equals("/msgconf"))
                {
                    s1=st.nextToken();
                    while(st.hasMoreTokens())s2+=st.nextToken()+" ";
                    msg(c2,c1.s,s1,"/msgconf " + "["+c1.nume+"]: "+s2);
                }
                else if (s3.equals("/confquit"))
                {
                    s1=st.nextToken();
                    while(st.hasMoreTokens())s2+=st.nextToken()+" ";
                    msg(c2,c1.s,s1,"/confquit " + c1.nume);
                }
                else if (s3.equals("/conf"))
                {
                    //if (st.hasMoreTokens()) s2 = st.nextToken();
                    String numePrieteni[] = new String [30];
                    int nrPrieteni;
                    nrPrieteni=0;
                    while (st.hasMoreTokens())
                    {
                        numePrieteni[nrPrieteni] = st.nextToken();
                        nrPrieteni++;
                    }
                    for (int i=0;i<nrPrieteni;i++)
                    {
                        s2="";
                        for (int j=0;j<nrPrieteni;j++)
                            if (j != i) s2+=numePrieteni[j] + " ";
                        msg(c2,c1.s,numePrieteni[i],"/invitatie " + c1.nume + " "+ s2);
                    }
                }
                else if (s3.equals("/bcast"))  bcast(c2,c1.s,"["+c1.nume+"]: "+mesaj.substring(7));
                else if (s3.equals("/nick"))
                {
                    if (unic(stmt1,mesaj.substring(6)))
                    {
                        if (isRightFormat(mesaj.substring(6))) nick(c1,mesaj.substring(6));
                        else if (mesaj.substring(6).length()>16) afiseaza(c1.s,"Porecla trebuie sa aiba maxim 16 caractere");
                        else afiseaza(c1.s,"In componenta poreclei nu puteti folosi alte caractere in afara de litere, cifre si - ");
                    }
                    else {
                        if (c1.nume.equals(mesaj.substring(6))) afiseaza(c1.s,"Ai deja aceasta porecla");
                        else afiseaza(c1.s,"Porecla este deja folosita de alt utilizator");
                    }
                }
                System.out.println("["+c1.nume+"]: " + mesaj);
            }
            catch (IOException e) {
                System.out.println("[eroare1]: " + e.getMessage());
            }
        }
    }
}

class Output extends Thread {
    DataOutputStream stream;
    Scanner scanner;
    Client c1;
    public Output(Client c[],int i) throws IOException {
        c1=c[i];
        stream = new DataOutputStream(c1.s.getOutputStream());
        scanner = new Scanner(System.in);
        start();
    }

    public void run() {
        String mesaj;

        while (c1.ok) {
            try {
                mesaj = scanner.nextLine();
                stream.writeUTF("[server]: " + mesaj);
            }
            catch (IOException e) {
                System.out.print("[eroare2]: " + e.getMessage());
            }
        }
    }
}
class Client
{
    public Socket s;
    public String nume="client",parola="";
    Boolean ok=true;
}
class V
{
    public static int n=1;
}

public class Server {
    /**
     * @param args the command line arguments
     */
public static ServerSocket server;
public static Connection con;
    public static int getPort()
    {
        return 1234;
    }
    
    public static void main(String[] args) {
        
        try {
            server = new ServerSocket(getPort());
            System.out.println("Serverul a pornit.");
        }
        catch (IOException e) {
            System.out.println("Nu pot porni serverul.");
            return;
        }
        try {
            //Persoana P;
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
        }
    String url = "jdbc:odbc:ClientiODBC";
    con = null;
        try {
            con = DriverManager.getConnection(url,"","");
        } catch (SQLException ex) {
            System.out.println("Nu ma pot conecta la baza de date");
        }
    Statement stmt = null;
        try {
            stmt = con.createStatement();
        } catch (SQLException ex) {
            System.out.println("Nu pot creea statement-ul");
        }
        Client c[]=new Client[39];
        while (true) {
            try {
                //c[1].s=new Socket();
                c[V.n]=new Client();
                c[V.n].s = server.accept();
                System.out.println("Avem un client");
                new Input(c,V.n,stmt);
                V.n++;
            }
            catch (IOException e) {
                System.out.println("Nu pot porni serverul.");
                return;
            }
           
        }


    }
@Override
protected void finalize(){
     try {
            con.close();
        } catch (SQLException ex) {
            System.out.println("sdfsdf");
        }
     try{
        server.close();
        System.out.println("Socket has been closed");
    } catch (IOException e) {
        System.out.println("Could not close socket");
        System.exit(-1);
    }
  }


}
