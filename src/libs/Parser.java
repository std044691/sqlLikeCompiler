package libs;

import java.util.ArrayList;

//Υλοποιεί την παρακάτω γραμματική 
//<program> 	::= <statement>1 ( ; <statement>2 )*
//<statement> 	::= select <select> | join <join>
//<select> 	::= <columns> from <table>1 
//			<where-part>
//			create <table>2 
//
//<where-part>	::= where <condition> | ε
//<columns> 	::= <column>1 ( , <column>2 )*
//<condition> 	::= <column> == <value>1 ( or <value>2 )*
//<join> 		::= <table>1 , <table>2 
//			where <column>1 == <column>2
//			create <table>3
//<table> 	::= string
//<column> 	::= string
//<value> 	::= “string”

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
    
        
        //Έναρξη του προγράμματος του parser. Εδώ υλοποιείτε ο κανόνας της γραμματικής
        //<program> 	::= <statement>1 ( ; <statement>2 )*
        private void program(){
            //Θέτω τον αριθμό των σφαλμάτων σε 0
            this.noOfErrors=0;
            //Τυπώνω απλά την έναρξη του συντακτικού ελέγχου
            System.out.println("Checking syntax..");
            //Εκτελώ την συνάρτηση statement όπως ορίζει η γραμματική
            statement();
            //Καθώς βρίσκω semicolons ";" προχωράω στην επόμενη εντολή
            while(token.type.name()=="semicolTK"){
                token = lex.nextToken();
                //Αν βρω το τέλος του αρχείου τότε βγαίνω. Δηλαδή σε περίπτωση που βρω ; και μετά τίποτα
                if(token.type.name()=="eofTK"){
                    break;
                }
                //Αλλιώς εκτελώ την επόμενη εντολή
                statement();    
            }
            
            //Απλά ένας έλεγχος για το αν υπάρχουν λάθει και τυπώνει στην οθόνη ότι τελείωσε ο έλεγχος.
            System.out.println("Checking syntax finished..");
            if(this.noOfErrors==0){
                System.out.println("Code is ok");
            }
            
            
            //Ελέγχω ποιο πεδίο δεν υπάρχει στον πίνακα.
            System.out.println("Executing..");  
            for(String s: selectedFields){
                if(!existingFields.contains(s)){
                    error("Field "+ s + " does not exist in tables");
                }
            }                                    
        }
        
        //Υλοποίηση του κανόνα
        //<statement> 	::= select <select> | join <join>
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
        
        //Υλοποίηση του κανόνα της γραμματικής
        ////<select> 	::= <columns> from <table>1 
        //                  <where-part>
        //		    create <table>2   
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
            
            new Table(table_name, from_table , selectedFields, this.selected_field_to_check, this.selected_values);
        }                
        
        //Υλοποίηση του κανόνα
        //<where-part>	::= where <condition> | ε
        private void where_part(){
            if(token.type.name()=="whereTK"){
                token = lex.nextToken();
                this.selected_field_to_check = token.data.toString();
                condition();                
            }else{
                ;
            }
        }  
        
        //Υλοποίηση του κανόνα
        //<columns> 	::= <column>1 ( , <column>2 )*
        private void columns(){
            this.selectedFields.add(column());
            
            while(token.type.name()=="commaTK"){
                token = lex.nextToken();
                this.selectedFields.add(column());
            }
        }
        
        //Υλοποίηση του κανόνα
        //<condition> 	::= <column> == <value>1 ( or <value>2 )*
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
                            this.createCommand = true;
                            table_name = new String(table());
                        } else error ("create expected");
                    } else error("symbol == expected");
                } else error("where expected");
            } else error("comma expected");
            
            new Table(table_name, table_1, table_2, selected_field_1, selected_field_2);
        }        
        
        //Υλοποίηση της γραμματικής <table> 	::= string με κάποιες προσθήκες
        private String table(){
            String val="";
            if(token.type.name()== "stringTK"){
                //Διαβάζουμε την τιμή του ονόματος του πίνακα
                val = token.data.toString();
                //Ελέγχω αν έρχομαι εδώ μετά από το from του select και όχι από το create. Αν έρχομαι μετά από from
                //Τότε σώζω τα πεδία του πίνακα για να τα ελέγξω αν υπάρχουν
                if(this.createCommand==false){
                    //Αν δεν υπάρχει ο πίνακας τότε θα κάνει error μέσα στο exception handling της κλάσης table
                    Table tb = new Table(val);
                    for(String s: tb.fields){
                        this.existingFields.add(s);
                    }
                }               
                token = lex.nextToken();                
            }else{
                error("Table name expeted");
            }
            return val;
        }
        
        //Υλοποίηση του 
        //<column> 	::= string
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
        
        
        //Υλοποίηση του 
        //<value> 	::= “string”
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
