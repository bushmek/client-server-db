package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


public class Server {
	
	static String url="jdbc:mysql://127.0.0.1:3306/java";
	static String selectAll="SELECT * FROM sunflower";
	static String selectSample="SELECT * FROM sunflower WHERE production >= 6500000";
	static String user="";
	static String pass="";
	private static Console console;
	private static ObjectInputStream in;
	private static ObjectOutputStream  out;
	private static Connection conn;
	
	
	public static void main(String[] args) throws ClassNotFoundException {
		 console = System.console();
		   if (console!=null) {
			
	       try {
	    	   ServerSocket s = new ServerSocket(12345);
	    	   System.out.println("Сервер запущено...");
	           Socket socket = s.accept();
	           System.out.println("Клієнт приєднався");
	           
	           in =new ObjectInputStream(
	                   socket.getInputStream());

	           out = new ObjectOutputStream(
	                   socket.getOutputStream());
	           
	           while(true){
	        	   System.out.println("по новой");
	        	   Object command=in.readObject();
	        	   System.out.println(command);
	        	   if(command.equals("exit")) {
	        		   break;
	        	   }
	        	   if(command.equals("login")) {
	        		   user=(String)in.readObject();
	        		   pass=(String)in.readObject();
	        		   conn=connect(url,user,pass);
	        	   }
	        	   if(command.equals("select")) {
	        		 ArrayList<Sunflower> q=select(selectAll);
	        		 out.writeObject(q.size());
	        		 for(int i=0;i<q.size();i++) {
	        			 out.writeObject(q.get(i));
	        		 }
	        	   }
	        	   
	        	   if(command.equals("insert")) {
	        		   Sunflower recive=(Sunflower)in.readObject();
	        		   insert(recive);
	        	   }
	        	   
	        	   if(command.equals("update")) {
	        		   Sunflower recive=(Sunflower)in.readObject();
	        		   update(recive);
	        	   }
	        	   
	        	   if(command.equals("delete")) {
	        		   int recive=(Integer)in.readObject();
	        		   delete(recive);
	        	   }
	        	   
	        	   if(command.equals("showsamp")) {
	        		   showAvrg(select(selectAll));
	        		   ArrayList<Sunflower> q=select(selectSample);
	        		   out.writeObject(q.size());
		        		 for(int i=0;i<q.size();i++) {
		        			 out.writeObject(q.get(i));
		        		 }
	        	   }
	           }
	           in.close();
	           out.close();
	           socket.close();
	           s.close();
	           System.out.println("Клієнт відєднався від серверу");
	           console.readLine();
	      } catch(IOException ex) {
	    	  ex.printStackTrace();
		  }
	   
		   }

	}
	
	public static Connection connect(String url,String user,String pass) throws IOException {
		conn=null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
			conn = DriverManager.getConnection(url, user, pass);
				console.printf("Connection to DB succesfull!\n");
				out.writeObject("suc");
			}
			catch(Exception ex) {
				console.printf("Error: %s \n",ex);
				out.writeObject("err");
			}
		return conn;
		}
	
	
	public static ArrayList<Sunflower> select(String query) {
		
		 ArrayList<Sunflower> sunflowers = new ArrayList<Sunflower>();
		 try {
		 if(conn!=null) {
		 Statement statement = conn.createStatement();
		 ResultSet resultSet = statement.executeQuery(query);
		 while(resultSet.next()){
     
            int year = resultSet.getInt(1);
            double production = resultSet.getDouble(2);
            double cost = resultSet.getDouble(3);
            Sunflower product = new Sunflower(year, production, cost);
            sunflowers.add(product);
        	 }
			}

		 }catch(Exception ex) {
			 console.printf("Error: %s \n",ex);
       	 //System.out.print("err2 "+ex);
		 }
		 return sunflowers;
	}
	
	public static int insert(Sunflower sunflower) throws IOException {
		try{
		if(conn!=null) {
			String sql = "INSERT INTO sunflower (year, production, cost) Values (?, ?, ?)";
		try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
			preparedStatement.setInt(1, sunflower.getYear());
			preparedStatement.setDouble(2, sunflower.getProduction());
			preparedStatement.setDouble(3, sunflower.getCost());
			preparedStatement.executeUpdate();
			System.out.println("added");
			out.writeObject("suc");
			}
		}
		}catch(Exception ex){
			//System.out.println(ex);
			console.printf("Error: %s \n",ex);
			out.writeObject("err");
			}
		return 0;
			
	}
	
	public static int update(Sunflower sunflower) throws IOException {
        
    	try{
    		if(conn!=null) {
            String sql = "UPDATE sunflower SET production = ?, cost = ? WHERE year = ?";
            try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
                preparedStatement.setDouble(1, sunflower.getProduction());
                preparedStatement.setDouble(2, sunflower.getCost());
                preparedStatement.setInt(3, sunflower.getYear());
                
                preparedStatement.executeUpdate();
              	System.out.println("updated");
      			out.writeObject("suc");
            }
    	}
    		
    }
    catch(Exception ex){
        //System.out.println(ex);
        console.printf("Error: %s \n",ex);
        out.writeObject("err");
    }
    return 0;
}

	public static int delete(int id) throws IOException {
      
		String sql = "DELETE FROM sunflower WHERE year = ?";
        try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
        	preparedStatement.setInt(1, id);      
            preparedStatement.executeUpdate();
            System.out.println("updated");
     		out.writeObject("suc");
        }
        catch(Exception ex){
            System.out.println(ex);
            out.writeObject("err");
        }
       
        return 0;
    }
	
	public static void showAvrg(ArrayList<Sunflower> sunflowers) throws IOException {
		double allProduction=0;
		for (int i=0;i<sunflowers.size();i++) {
			allProduction+=sunflowers.get(i).getProduction();
		}
		double res=allProduction/sunflowers.size();
		out.writeObject(res);
	}

}
