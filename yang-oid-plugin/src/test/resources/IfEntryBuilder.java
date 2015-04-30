public class IfEntryBuilder {

    private java.lang.String _ifDescr;
    private java.lang.Integer _ifMtu;

    public IfEntryBuilder() {
    }

    public java.lang.String getIfDescr() {
        return _ifDescr;
    }

    public java.lang.Integer getIfMtu() {
        return _ifMtu;
    }

    public IfEntryBuilder setIfDescr(java.lang.String value) {
        this._ifDescr = value;
        return this;
    }

    public IfEntryBuilder setIfMtu(java.lang.Integer value) {
        this._ifMtu = value;
        return this;
    }

    public IfEntryImpl build() {
        return new IfEntryImpl(this);
    }

    private static final class IfEntryImpl {

        private final java.lang.String _ifDescr;
        private final java.lang.Integer _ifMtu;

        private IfEntryImpl(IfEntryBuilder base) {
            this._ifDescr = base.getIfDescr();
            this._ifMtu = base.getIfMtu();
        }

        public java.lang.String getIfDescr() {
            return _ifDescr;
        }

        public java.lang.Integer getIfMtu() {
            return _ifMtu;
        }
    }

}
