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
    
    public Table(String table_name, Table from_table, ArrayList<String> selected_fields_to_present, String selected_field_to_check, ArrayList<String> selected_values, Table joinedTable, String selected_field_1, String selected_field_2) 
    // create table from select with join
    {
        name = new String(table_name);        
        //fields.add("recordID");

        ArrayList<Integer> colPositions = new ArrayList<Integer>();
        Integer col=0;
        
        for (String f: from_table.fields){
            if(selected_fields_to_present.contains(f)){
                fields.add(from_table.name+"_"+f);                
                //fields.add(f);
                colPositions.add(col);
                
            }
            col++;
        }
        for (String f: joinedTable.fields){
            if(selected_fields_to_present.contains(f)){
                fields.add(joinedTable.name+"_"+f);
                //fields.add(f);
                colPositions.add(col);
            }
            col++;
        }
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
        for (int i=0; i<from_table.data.size(); i++)
        {
            for (int j=0; j<joinedTable.data.size(); j++)
            {
                if (from_table.data.get(i).get(pos_1).equals(joinedTable.data.get(j).get(pos_2)))
                {
                    ArrayList<String> record = new ArrayList<String>();
                    //record.add(String.valueOf(primary_key));
                                        
                    for (String s:from_table.data.get(i)){
                            if(colPositions.contains(col)){
                                record.add(s);                 
                            }
                            col++;
                    }
                    
                    for (String s:joinedTable.data.get(j)){                        
                            if(colPositions.contains(col)){
                                record.add(s);                 
                            }
                            col++;
                    }
                    
                    primary_key++;
                    col=0;
                    data.add(record);
                }
            }
        }
        this.tableToCsv();
        
        
    }   
    
    public Table(String table_name, Table from_table, ArrayList<String> selected_fields_to_present, String selected_field_to_check, ArrayList<String> selected_values) 
    // create table from select
    {
        
        name = new String(table_name);
        ArrayList<Integer> colPositions = new ArrayList<Integer>();

        for (String f: selected_fields_to_present){
            fields.add(f);            
            System.out.print(f + " ");
        }
        System.out.println("");
        System.out.println("---------------");
        
        for (String f: selected_fields_to_present){
            colPositions.add(from_table.fields.indexOf(f));
        }
        
        
        if(selected_field_to_check!=""){
            int pos = from_table.fields.indexOf(selected_field_to_check); 
            if (pos==-1){
                System.out.println("invalid field name ("+selected_field_to_check+") for select ");
                System.exit(0);
            }
                        
            for(ArrayList<String> row : from_table.data){
                ArrayList<String> record = new ArrayList<String>();
                
                for(int colPos :colPositions){                
                    if (selected_values.contains(row.get(pos))){
                        System.out.print(row.get(colPos)+ " ");
                        record.add(row.get(colPos));
                    }
                }
                data.add(record);
                
                if (selected_values.contains(row.get(pos))){
                    System.out.println("");
                }
            }
        }else{
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
        
        
        
        
        
            
//        for (int i=0; i<from_table.data.size(); i++)
//        {
//
//            if (selected_values.contains(from_table.data.get(i).get(pos))){
//                ArrayList<String> record = new ArrayList<String>();                                
//                for(int j=0;j<from_table.data.get(i).size();j++){
//                    if(colPositions.contains(j)){
//                        record.add(from_table.data.get(i).get(j));
//                    }
//                }
//                
//                data.add(record);
//            }
//        }
        
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
