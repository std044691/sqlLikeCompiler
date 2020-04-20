package libs;


public enum TokenType {    
        whitespaceTK("[ \t\f\r]+"),
        newlineTK("\n"),
        selectTK("select"),
        joinTK("join"),
        fromTK("from"),
        createTK("create"),
        whereTK("where"),
        orTK("or"),
        equalTK("=="),       
        semicolTK(";"),
        commaTK(","),
        eofTK("\\Z"),
        quoatedStringTK("\"[a-zA-ZΑ-Ωα-ω]*\""),
        stringTK("[a-zA-Z_]*"),
        unknownTK("."),
        ;

        public final String pattern;

        private TokenType(String pattern) {
            this.pattern = pattern;
        }    
}
