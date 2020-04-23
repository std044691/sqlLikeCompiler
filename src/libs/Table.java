package libs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Table 
{
    
    String name;
    ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String> >();
    ArrayList<String> fields = new ArrayList<String>();

    private static final int ALL_FIELDS = -1;
    private static final String dbPath= "src/db/";
        
    public Table(String table_name)  
    // create table from file  
    {
        name = new String(table_name);
        try  
        {
            String filename = this.dbPath+name+".csv";
            File file = new File(filename);    
            FileReader fr = new FileReader(file);   
            BufferedReader br = new BufferedReader(fr);   
            String line;
            
            boolean at_fields = true;
            while((line=br.readLine())!=null)  
            {   
                if (at_fields)
                {
                    at_fields = false;
                    String[] values = line.split(",");
                    for (String s: values)
                        fields.add(s);
                }
                else  
                {
                    data.add(new ArrayList<String>());
                    String[] values = line.split(",");
                    for (String s: values)
                        data.get(data.size()-1).add(s);
                }
            }  
            fr.close();  
        }  
        catch(IOException e)  
        {  
            //System.out.println("Exception on csv file reading");
            System.out.println("Table "+name+" doesnt exist");
            System.exit(0);
        }  
    }
    
    //Πήγα να αλλάξω την γραμματική να πειραματιστώ λίγο 
    public Table(String table_name, Table from_table, ArrayList<String> selected_fields_to_present, String selected_field_to_check, ArrayList<String> selected_values, Table joinedTable, String selected_field_1, String selected_field_2) 
    // create table from select with join
    {
        name = new String(table_name);        
        //fields.add("recordID");

        ArrayList<Integer> colPosFromTable = new ArrayList<Integer>();
        ArrayList<Integer> colPosJoinTable = new ArrayList<Integer>();
        Integer col=0;
        
        for (String f: selected_fields_to_present){
            if(from_table.fields.contains(f)){
                //fields.add(from_table.name+"_"+f);
                fields.add(f);
                colPosFromTable.add(col);
                System.out.print(f + " ");
            }
            col++;            
        }
        
        col=0;
        for (String f: selected_fields_to_present){
            if(from_table.fields.contains(f) && !fields.contains(f)){
                //fields.add(joinedTable.name+"_"+f);                
                fields.add(f);
                colPosJoinTable.add(col);
                System.out.print(f+ " ");
            }
            col++;
        }
        System.out.println("");
        System.out.println("----------------");
        //System.out.println(colPositions);
        
        int pos_1 = from_table.fields.indexOf(selected_field_1);
        int pos_2 = joinedTable.fields.indexOf(selected_field_2);

        if (pos_1==-1 || pos_2==-1)
        {
            System.out.println("invalid field name given in join");
            System.exit(0);
        }
        

        int primary_key = 1;
        col=0;
        
        for(ArrayList<String> fromTableRow : from_table.data){
            for(ArrayList<String> joinedTableRow : joinedTable.data){
                if (fromTableRow.get(pos_1).equals(joinedTableRow.get(pos_2))){
                    for(int i: colPosFromTable){
                        System.out.print(fromTableRow.get(i)+" ");
                    }
                    
                    for(int i: colPosJoinTable){
                        System.out.print(joinedTableRow.get(i)+" ");
                    }
                    
                    System.out.println("");
                }                
            }
        }                   
    }   
    
    public Table(String table_name, Table from_table, ArrayList<String> selected_fields_to_present, String selected_field_to_check, ArrayList<String> selected_values) 
    // create table from select
    {     
        //Το όνομα του αρχείου CSV 
        name = new String(table_name);
        //Δήλωση των θέσεων των στοιχείων του πίνακα. Θα μπορούσα να το κάνω με hashmap αλλά οκ..
        ArrayList<Integer> colPositions = new ArrayList<Integer>();

        //Καταχώρηση των θέσεων των στηλών του πίνακα που επιλέγονται να εμφανιστούν
        for (String f: selected_fields_to_present){
            //Καταχώρηση του πεδίου στην λίστα των πεδίων που χρησιμοποιείτε για την δημιουργία του αρχείου
            fields.add(f);            
            
            //Καταχώρηση της θέσης της κολόνας από το αρχείο
            colPositions.add(from_table.fields.indexOf(f));
            //Τυπικό print να δούμε τι θα γραφτεί στο αρχείο
            System.out.print(f + " ");
        }
        
        //Απλά ένα κενό να χωρίζουν οι τίτλοι των πεδίων από τα πεδία με μια γραμμή.. Απλά για το print δεν έχει να κάνει με το αρχείο
        System.out.println("");
        System.out.println("---------------");

        //Ελέγχουμε το πεδί του where να δούμε αν υπάρχει ώστε να επιλέξουμε τα πεδία σύμφωνα με την συνθήκη. Αλλιώς τα επιλέγουμε όλα.        
        if(selected_field_to_check!=""){
            //Βρίσκουμε την θέση του πεδίου που θέλουμε να ελέγξουμε στο αρχείο
            int pos = from_table.fields.indexOf(selected_field_to_check); 
            
            //Αν δεν υπάρχει στο αρχείο τότε σκάμε με το ακόλουθο μήνυμα
            if (pos==-1){
                System.out.println("invalid field name ("+selected_field_to_check+") for select ");
                System.exit(0);
            }
                        
            //Λούπα για κάθε γραμμή των δεδομέων του αρχείου
            for(ArrayList<String> row : from_table.data){
                //Δημιουργώ ένα record
                ArrayList<String> record = new ArrayList<String>();
                
                //Για κάθε κολόνα που είναι να εμφανίσω
                for(int colPos :colPositions){                
                    //ελέγχω αν η κολόνα υπάρχει στην γραμμή
                    if (selected_values.contains(row.get(pos))){
                        //Την τυπώνω.. στο αρχείο και στην οθόνη. Ίσως εδώ καλύτερα να γινόταν η δουλειά με hashmap
                        System.out.print(row.get(colPos)+ " ");
                        record.add(row.get(colPos));
                    }
                }                
                
                //Καταγράφω την γραμμή στο αρχείο-πίνακα και τυπώνω ένα κενό στην οθόνη
                if (selected_values.contains(row.get(pos))){
                    System.out.println("");
                    data.add(record);
                }
            }
        }else{
            
            //Απλά τυπώνω τα πάντα στο αρχείο και στην οθόνη χωρίς έλεγχο με την σωστή σειρά
            for(ArrayList<String> row : from_table.data){
                ArrayList<String> record = new ArrayList<String>();
                
                for(int colPos :colPositions){                                    
                    System.out.print(row.get(colPos)+ " ");
                    record.add(row.get(colPos));
                }     
                data.add(record);
                System.out.println("");                
            }
        }        
        this.tableToCsv();        
    }
    
    public Table(String table_name, Table table_1, Table table_2, String selected_field_1, String selected_field_2) 
    // create table from join
    {
        name = new String(table_name);
        fields.add("recordID");

        
        for (String f: table_1.fields)
            fields.add(table_1.name+"_"+f);
        for (String f: table_2.fields)
            fields.add(table_2.name+"_"+f);
        
        int pos_1 = table_1.fields.indexOf(selected_field_1);
        int pos_2 = table_2.fields.indexOf(selected_field_2);
        
        if (pos_1==-1 || pos_2==-1)
        {
            System.out.println("invalid field name given in join");
            System.exit(0);
        }
           
        int primary_key = 1;
        for (int i=0; i<table_1.data.size(); i++)
        {
            for (int j=0; j<table_2.data.size(); j++)
            {
                if (table_1.data.get(i).get(pos_1).equals(table_2.data.get(j).get(pos_2)))
                {
                    ArrayList<String> record = new ArrayList<String>();
                    record.add(String.valueOf(primary_key));
                    for (String s:table_1.data.get(i))
                        record.add(s);
                    for (String s:table_2.data.get(j))
                        record.add(s);
                    primary_key++;
                    data.add(record);
                }
            }
        }
        this.tableToCsv();
    }

    
    public String toString()
    {
        return "---\n"+name+"\n"+fields.toString()+"\n"+data.toString()+"\n---";
    }  
        
    
    public void tableToCsv()
    {
        try 
        {
            String filename = this.dbPath+name+".csv";
            FileWriter myWriter = new FileWriter(filename);
            
            for (int i=0; i<fields.size()-1;i++)
            {
                myWriter.write(fields.get(i));
                myWriter.write(",");
            }
            myWriter.write(fields.get(fields.size()-1));
            myWriter.write("\n");
                    
            for (int i=0; i<data.size();i++)
            {
                for (int j=0; j<data.get(i).size()-1;j++)
                {
                    myWriter.write(data.get(i).get(j));
                    myWriter.write(",");
                }
                myWriter.write(data.get(i).get(data.get(i).size()-1));
                myWriter.write("\n"); 
            }     
            myWriter.close();
      
        } 
        catch (IOException e) 
        {  
            System.out.println("Exception on csv file writing");
            System.exit(0);
        } 
    }
    
    
    
   
}
