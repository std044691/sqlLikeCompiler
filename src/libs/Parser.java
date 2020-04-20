package libs;

import java.util.ArrayList;


public class Parser {
        private Lex lex;   
        private Token token;
        private int noOfErrors;
        private Boolean createCommand;
        private ArrayList<String> selectedFields;
        private ArrayList<String> existingFields;
        private String tableToCreate;
        private String selected_field_to_check="";       
        private ArrayList<String> selected_values;
        
        public Parser (String filename){
            this.lex = new Lex(filename);            
            this.token = lex.nextToken();
            this.createCommand = false;
            this.selectedFields = new ArrayList<String>();
            this.existingFields = new ArrayList<String>();
            this.tableToCreate = "";
            this.selected_field_to_check = "";
            this.selected_values = new ArrayList<String>();
            
            program();  
        }
       
        
        private void error(String s){
            System.out.format("error in line %d: %s\n",token.line,s);
            this.noOfErrors++;
            System.exit(0);
        }
    
        
        private void program(){
            this.noOfErrors=0;
            System.out.println("Compiling..");
            statement();
            while(token.type.name()=="semicolTK"){
                token = lex.nextToken();
                statement();    
            }
            
            System.out.println("Compiling finished..");
            if(this.noOfErrors==0){
                System.out.println("Successfull compiling");
            }
            
            
            System.out.println("Executing..");
            for(String s: selectedFields){                
                if(!existingFields.contains(s)){
                    error("Field "+ s + " does not exist in tables");
                }
            }
                        
            
        }
        
        private void statement(){            
            if(token.type.name()=="selectTK"){
                token = lex.nextToken();
                select();
            }else if(token.type.name()=="joinTK"){
                token = lex.nextToken();
                join();                
            }else{                
                error(token.type.name());
            }
        }
        
        private void select(){            
            String table_name = "";
            Table from_table = null;
                        
            
            columns();
            if(token.type.name()=="fromTK"){
                this.createCommand=false;
                token = lex.nextToken();
                //table();
                from_table = new Table(table());
                
                where_part();
                
                if(token.type.name()=="createTK"){
                    this.createCommand=true;
                    token = lex.nextToken();
                    table_name = token.data;
                    table();                    
                    
                }else{
                    error(token.type.name());
                }
            }else{
                error(token.type.name());
            }
            
            //new Table(
            //String table_name, OK
            //Table from_table,  OK
            //ArrayList<String> selected_fields_to_present, OK
            //String selected_field_to_check, OK
            //ArrayList<String> selected_values); OK
            
            new Table(table_name, from_table , selectedFields, this.selected_field_to_check, this.selected_values);
        }                
        
        private void where_part(){
            if(token.type.name()=="whereTK"){
                token = lex.nextToken();
                this.selected_field_to_check = token.data.toString();
                condition();                
            }else{
                ;
            }
        }  
        private void columns(){            
            column();
            while(token.type.name()=="commaTK"){
                token = lex.nextToken();
                column();
            }
        }
        
        private void condition(){            
            column();
            if(token.type.name()== "equalTK"){
                token = lex.nextToken();
                
                selected_values.add(value());
                while(token.type.name()=="orTK"){
                    token = lex.nextToken();                   
                    selected_values.add(value());
                }
            }else{
                error("error");
            }          
        }
        
        private void join()
        {
            String table_name="";
            Table table_1 = null;
            Table table_2 = null;
            String selected_field_1="";
            String selected_field_2="";

            table_1 = new Table(table());
            
            if (token.type.name() == "commaTK"){
                token = lex.nextToken();
                table_2 = new Table(table());
                if (token.type.name() == "whereTK"){
                    token = lex.nextToken();
                    selected_field_1=column();
                    if (token.type.name() == "equalTK"){
                        token = lex.nextToken();
                        selected_field_2=column();
                        if (token.type.name() == "createTK"){
                            token = lex.nextToken();
                            table_name = new String(table());
                        } else error ("create expected");
                    } else error("symbol == expected");
                } else error("where expected");
            } else error("comma expected");
            
            new Table(table_name, table_1, table_2, selected_field_1, selected_field_2);
        }        
        
        private String table(){
            String val="";
            if(token.type.name()== "stringTK"){
                val = token.data.toString();
                if(this.createCommand==false){
                    Table tb = new Table(val);
                    for(String s: tb.fields){
                        this.existingFields.add(s);                        
                    }
                }
                                
                val = token.data.toString();
                token = lex.nextToken();
                
            }else{
                error("error");
            }
            return val;
        }
        
        private String column(){
            String val="";
            if(token.type.name()== "stringTK"){                
                val = token.data.toString();
                this.selectedFields.add(val);                
                token = lex.nextToken();
            }else{
                error("error");
            }
            return val;
        } 
        
        private String value(){
            String val="";
            if(token.type.name()== "quoatedStringTK"){
                val = token.data.toString().replace("\"", "");
                token = lex.nextToken();                
            }else{
                error("error");
            }
            return val;
        }
        
}