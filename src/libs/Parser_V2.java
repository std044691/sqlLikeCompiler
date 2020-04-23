package libs;

import java.util.ArrayList;

//Υλοποιεί την παρακάτω γραμματική 
//<program> 	::= <statement>1 ( ; <statement>2 )*
//<statement> 	::= select <select> | join <join>
//<select> 	::= <columns> from <table>1 
//			<join-part>
//			<where-part>			
//			<create> 
//<join-part>	::= join <table>2 on <column>1 == <column>2|ε
//<where-part>	::= where <condition> | ε
//<columns> 	::= <column>1 ( , <column>2 )*
//<condition> 	::= <column> == <value>1 ( or <value>2 )*
//<join> 		::= <table>1 , <table>2 
//			where <column>1 == <column>2
//			<create>
//
//<create>	::= create <table>3 |ε
//<table> 	::= string
//<column> 	::= string
//<value> 	::= “string”

public class Parser_V2 {
        private Lex lex;   
        private Token token;
        private int noOfErrors;
        private Boolean createCommand;
        private ArrayList<String> selectedFields;
        private ArrayList<String> existingFields;
        private String tableToCreate;
        private String selected_field_to_check="";       
        private ArrayList<String> selected_values;
        private Table joinedTable;
        private String selected_field_1, selected_field_2;
        private String tableName;
        
        
        public Parser_V2 (String filename){
            this.lex = new Lex(filename);            
            this.token = lex.nextToken();
            this.createCommand = false;
            this.selectedFields = new ArrayList<String>();
            this.existingFields = new ArrayList<String>();
            this.tableToCreate = "";
            this.selected_field_to_check = "";
            this.tableName = "";
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
                if(token.type.name()=="eofTK"){
                    break;
                }
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
            
            Table from_table = null;
            Boolean hasJoin = false;                                                            
            columns();
            if(token.type.name()=="fromTK"){
                this.createCommand=false;
                token = lex.nextToken();
            
                //table();
                from_table = new Table(table());
                hasJoin = join_part();
                where_part();
                create();                
            }else{
                error(token.type.name());
            }
            
            //new Table(
            //String table_name, OK
            //Table from_table,  OK
            //ArrayList<String> selected_fields_to_present, OK
            //String selected_field_to_check, OK
            //ArrayList<String> selected_values); OK
            
            if(hasJoin){
                if(this.createCommand)
                    new Table(this.tableName, from_table , selectedFields, this.selected_field_to_check, this.selected_values, this.joinedTable, this.selected_field_1, this.selected_field_2);
            }
            else{
                if(this.createCommand)
                    new Table(this.tableName, from_table , selectedFields, this.selected_field_to_check, this.selected_values);
            }
        }                
        
        private void create(){
            if(token.type.name()=="createTK"){
                this.createCommand=true;
                token = lex.nextToken();
                this.tableName = table();
            }else{                
                ;
            }
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
            //column();
            this.selectedFields.add(column());
            while(token.type.name()=="commaTK"){
                token = lex.nextToken();
                this.selectedFields.add(column());
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
        
        private Boolean join_part(){
            String table_name = "";           
            Boolean hasJoin = false;
                                   
            if (token.type.name() == "joinTK"){
                hasJoin = true;
                token = lex.nextToken();
                this.joinedTable = new Table(table());
                if(token.type.name()=="onTK"){
                    token = lex.nextToken();
                    this.selected_field_1=column();
                    if (token.type.name() == "equalTK"){
                        token = lex.nextToken();
                        this.selected_field_2=column();
                    } else error("symbol == expected");
                }else{
                    error("table expected for join");
                }                
            } else{                
                ;
            } 
            
            return hasJoin;            
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
                            this.createCommand = true;
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
