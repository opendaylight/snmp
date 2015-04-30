package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group;
import com.google.common.collect.Range;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import java.util.HashMap;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.PhysAddress;
import com.google.common.collect.ImmutableList;
import java.math.BigInteger;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2.ianaiftype.mib.rev100211.IANAifType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry.IfAdminStatus;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.ObjectIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Gauge32;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry.IfOperStatus;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.InterfaceIndex;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Timeticks;
import org.opendaylight.snmp.OID;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry
 *
 */
public class IfEntryBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry> {

    private IfAdminStatus _ifAdminStatus;
    private java.lang.String _ifDescr;
    private static List<Range<BigInteger>> _ifDescr_length;
    private Counter32 _ifInDiscards;
    private static List<Range<BigInteger>> _ifInDiscards_range;
    private Counter32 _ifInErrors;
    private static List<Range<BigInteger>> _ifInErrors_range;
    private Counter32 _ifInNUcastPkts;
    private static List<Range<BigInteger>> _ifInNUcastPkts_range;
    private Counter32 _ifInOctets;
    private static List<Range<BigInteger>> _ifInOctets_range;
    private Counter32 _ifInUcastPkts;
    private static List<Range<BigInteger>> _ifInUcastPkts_range;
    private Counter32 _ifInUnknownProtos;
    private static List<Range<BigInteger>> _ifInUnknownProtos_range;
    private InterfaceIndex _ifIndex;
    private static List<Range<BigInteger>> _ifIndex_range;
    private Timeticks _ifLastChange;
    private static List<Range<BigInteger>> _ifLastChange_range;
    private java.lang.Integer _ifMtu;
    private IfOperStatus _ifOperStatus;
    private Counter32 _ifOutDiscards;
    private static List<Range<BigInteger>> _ifOutDiscards_range;
    private Counter32 _ifOutErrors;
    private static List<Range<BigInteger>> _ifOutErrors_range;
    private Counter32 _ifOutNUcastPkts;
    private static List<Range<BigInteger>> _ifOutNUcastPkts_range;
    private Counter32 _ifOutOctets;
    private static List<Range<BigInteger>> _ifOutOctets_range;
    private Gauge32 _ifOutQLen;
    private static List<Range<BigInteger>> _ifOutQLen_range;
    private Counter32 _ifOutUcastPkts;
    private static List<Range<BigInteger>> _ifOutUcastPkts_range;
    private PhysAddress _ifPhysAddress;
    private ObjectIdentifier _ifSpecific;
    private Gauge32 _ifSpeed;
    private static List<Range<BigInteger>> _ifSpeed_range;
    private IANAifType _ifType;
    private IfEntryKey _key;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>> augmentation = new HashMap<>();

    public IfEntryBuilder() {
    }

    public IfEntryBuilder(IfEntry base) {
        if (base.getKey() == null) {
            this._key = new IfEntryKey(
                    base.getIfIndex()
            );
            this._ifIndex = base.getIfIndex();
        } else {
            this._key = base.getKey();
            this._ifIndex = _key.getIfIndex();
        }
        this._ifAdminStatus = base.getIfAdminStatus();
        this._ifDescr = base.getIfDescr();
        this._ifInDiscards = base.getIfInDiscards();
        this._ifInErrors = base.getIfInErrors();
        this._ifInNUcastPkts = base.getIfInNUcastPkts();
        this._ifInOctets = base.getIfInOctets();
        this._ifInUcastPkts = base.getIfInUcastPkts();
        this._ifInUnknownProtos = base.getIfInUnknownProtos();
        this._ifLastChange = base.getIfLastChange();
        this._ifMtu = base.getIfMtu();
        this._ifOperStatus = base.getIfOperStatus();
        this._ifOutDiscards = base.getIfOutDiscards();
        this._ifOutErrors = base.getIfOutErrors();
        this._ifOutNUcastPkts = base.getIfOutNUcastPkts();
        this._ifOutOctets = base.getIfOutOctets();
        this._ifOutQLen = base.getIfOutQLen();
        this._ifOutUcastPkts = base.getIfOutUcastPkts();
        this._ifPhysAddress = base.getIfPhysAddress();
        this._ifSpecific = base.getIfSpecific();
        this._ifSpeed = base.getIfSpeed();
        this._ifType = base.getIfType();
        if (base instanceof IfEntryImpl) {
            IfEntryImpl impl = (IfEntryImpl) base;
            this.augmentation = new HashMap<>(impl.augmentation);
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>) base;
            this.augmentation = new HashMap<>(casted.augmentations());
        }
    }


    public IfAdminStatus getIfAdminStatus() {
        return _ifAdminStatus;
    }

    public java.lang.String getIfDescr() {
        return _ifDescr;
    }

    public Counter32 getIfInDiscards() {
        return _ifInDiscards;
    }

    public Counter32 getIfInErrors() {
        return _ifInErrors;
    }

    public Counter32 getIfInNUcastPkts() {
        return _ifInNUcastPkts;
    }

    public Counter32 getIfInOctets() {
        return _ifInOctets;
    }

    public Counter32 getIfInUcastPkts() {
        return _ifInUcastPkts;
    }

    public Counter32 getIfInUnknownProtos() {
        return _ifInUnknownProtos;
    }

    public InterfaceIndex getIfIndex() {
        return _ifIndex;
    }

    public Timeticks getIfLastChange() {
        return _ifLastChange;
    }

    public java.lang.Integer getIfMtu() {
        return _ifMtu;
    }

    public IfOperStatus getIfOperStatus() {
        return _ifOperStatus;
    }

    public Counter32 getIfOutDiscards() {
        return _ifOutDiscards;
    }

    public Counter32 getIfOutErrors() {
        return _ifOutErrors;
    }

    public Counter32 getIfOutNUcastPkts() {
        return _ifOutNUcastPkts;
    }

    public Counter32 getIfOutOctets() {
        return _ifOutOctets;
    }

    public Gauge32 getIfOutQLen() {
        return _ifOutQLen;
    }

    public Counter32 getIfOutUcastPkts() {
        return _ifOutUcastPkts;
    }

    public PhysAddress getIfPhysAddress() {
        return _ifPhysAddress;
    }

    public ObjectIdentifier getIfSpecific() {
        return _ifSpecific;
    }

    public Gauge32 getIfSpeed() {
        return _ifSpeed;
    }

    public IANAifType getIfType() {
        return _ifType;
    }

    public IfEntryKey getKey() {
        return _key;
    }

    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public IfEntryBuilder setIfAdminStatus(IfAdminStatus value) {
        this._ifAdminStatus = value;
        return this;
    }

    public IfEntryBuilder setIfDescr(java.lang.String value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.length());
            boolean isValidLength = false;
            for (Range<BigInteger> r : _ifDescr_length()) {
                if (r.contains(_constraint)) {
                    isValidLength = true;
                }
            }
            if (!isValidLength) {
                throw new IllegalArgumentException(String.format("Invalid length: %s, expected: %s.", value, _ifDescr_length));
            }
        }
        this._ifDescr = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifDescr_length() {
        if (_ifDescr_length == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifDescr_length == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(255L)));
                    _ifDescr_length = builder.build();
                }
            }
        }
        return _ifDescr_length;
    }

    public IfEntryBuilder setIfInDiscards(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifInDiscards_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifInDiscards_range));
            }
        }
        this._ifInDiscards = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifInDiscards_range() {
        if (_ifInDiscards_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifInDiscards_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifInDiscards_range = builder.build();
                }
            }
        }
        return _ifInDiscards_range;
    }

    public IfEntryBuilder setIfInErrors(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifInErrors_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifInErrors_range));
            }
        }
        this._ifInErrors = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifInErrors_range() {
        if (_ifInErrors_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifInErrors_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifInErrors_range = builder.build();
                }
            }
        }
        return _ifInErrors_range;
    }

    public IfEntryBuilder setIfInNUcastPkts(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifInNUcastPkts_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifInNUcastPkts_range));
            }
        }
        this._ifInNUcastPkts = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifInNUcastPkts_range() {
        if (_ifInNUcastPkts_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifInNUcastPkts_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifInNUcastPkts_range = builder.build();
                }
            }
        }
        return _ifInNUcastPkts_range;
    }

    public IfEntryBuilder setIfInOctets(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifInOctets_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifInOctets_range));
            }
        }
        this._ifInOctets = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifInOctets_range() {
        if (_ifInOctets_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifInOctets_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifInOctets_range = builder.build();
                }
            }
        }
        return _ifInOctets_range;
    }

    public IfEntryBuilder setIfInUcastPkts(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifInUcastPkts_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifInUcastPkts_range));
            }
        }
        this._ifInUcastPkts = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifInUcastPkts_range() {
        if (_ifInUcastPkts_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifInUcastPkts_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifInUcastPkts_range = builder.build();
                }
            }
        }
        return _ifInUcastPkts_range;
    }

    public IfEntryBuilder setIfInUnknownProtos(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifInUnknownProtos_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifInUnknownProtos_range));
            }
        }
        this._ifInUnknownProtos = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifInUnknownProtos_range() {
        if (_ifInUnknownProtos_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifInUnknownProtos_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifInUnknownProtos_range = builder.build();
                }
            }
        }
        return _ifInUnknownProtos_range;
    }

    public IfEntryBuilder setIfIndex(InterfaceIndex value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifIndex_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifIndex_range));
            }
        }
        this._ifIndex = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifIndex_range() {
        if (_ifIndex_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifIndex_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ONE, BigInteger.valueOf(2147483647L)));
                    _ifIndex_range = builder.build();
                }
            }
        }
        return _ifIndex_range;
    }

    public IfEntryBuilder setIfLastChange(Timeticks value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifLastChange_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifLastChange_range));
            }
        }
        this._ifLastChange = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifLastChange_range() {
        if (_ifLastChange_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifLastChange_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifLastChange_range = builder.build();
                }
            }
        }
        return _ifLastChange_range;
    }

    public IfEntryBuilder setIfMtu(java.lang.Integer value) {
        this._ifMtu = value;
        return this;
    }

    public IfEntryBuilder setIfOperStatus(IfOperStatus value) {
        this._ifOperStatus = value;
        return this;
    }

    public IfEntryBuilder setIfOutDiscards(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifOutDiscards_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifOutDiscards_range));
            }
        }
        this._ifOutDiscards = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifOutDiscards_range() {
        if (_ifOutDiscards_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifOutDiscards_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifOutDiscards_range = builder.build();
                }
            }
        }
        return _ifOutDiscards_range;
    }

    public IfEntryBuilder setIfOutErrors(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifOutErrors_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifOutErrors_range));
            }
        }
        this._ifOutErrors = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifOutErrors_range() {
        if (_ifOutErrors_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifOutErrors_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifOutErrors_range = builder.build();
                }
            }
        }
        return _ifOutErrors_range;
    }

    public IfEntryBuilder setIfOutNUcastPkts(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifOutNUcastPkts_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifOutNUcastPkts_range));
            }
        }
        this._ifOutNUcastPkts = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifOutNUcastPkts_range() {
        if (_ifOutNUcastPkts_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifOutNUcastPkts_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifOutNUcastPkts_range = builder.build();
                }
            }
        }
        return _ifOutNUcastPkts_range;
    }

    public IfEntryBuilder setIfOutOctets(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifOutOctets_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifOutOctets_range));
            }
        }
        this._ifOutOctets = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifOutOctets_range() {
        if (_ifOutOctets_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifOutOctets_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifOutOctets_range = builder.build();
                }
            }
        }
        return _ifOutOctets_range;
    }

    public IfEntryBuilder setIfOutQLen(Gauge32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifOutQLen_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifOutQLen_range));
            }
        }
        this._ifOutQLen = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifOutQLen_range() {
        if (_ifOutQLen_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifOutQLen_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifOutQLen_range = builder.build();
                }
            }
        }
        return _ifOutQLen_range;
    }

    public IfEntryBuilder setIfOutUcastPkts(Counter32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifOutUcastPkts_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifOutUcastPkts_range));
            }
        }
        this._ifOutUcastPkts = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifOutUcastPkts_range() {
        if (_ifOutUcastPkts_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifOutUcastPkts_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifOutUcastPkts_range = builder.build();
                }
            }
        }
        return _ifOutUcastPkts_range;
    }

    public IfEntryBuilder setIfPhysAddress(PhysAddress value) {
        this._ifPhysAddress = value;
        return this;
    }

    public IfEntryBuilder setIfSpecific(ObjectIdentifier value) {
        this._ifSpecific = value;
        return this;
    }

    public IfEntryBuilder setIfSpeed(Gauge32 value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value.getValue());
            boolean isValidRange = false;
            for (Range<BigInteger> r : _ifSpeed_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _ifSpeed_range));
            }
        }
        this._ifSpeed = value;
        return this;
    }
    public static List<Range<BigInteger>> _ifSpeed_range() {
        if (_ifSpeed_range == null) {
            synchronized (IfEntryBuilder.class) {
                if (_ifSpeed_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _ifSpeed_range = builder.build();
                }
            }
        }
        return _ifSpeed_range;
    }

    public IfEntryBuilder setIfType(IANAifType value) {
        this._ifType = value;
        return this;
    }

    public IfEntryBuilder setKey(IfEntryKey value) {
        this._key = value;
        return this;
    }

    public IfEntryBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public IfEntryBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>> augmentationType) {
        this.augmentation.remove(augmentationType);
        return this;
    }

    public IfEntry build() {
        return new IfEntryImpl(this);
    }

    private static final class IfEntryImpl implements IfEntry {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry.class;
        }

        private final IfAdminStatus _ifAdminStatus;
        private final java.lang.String _ifDescr;
        private final Counter32 _ifInDiscards;
        private final Counter32 _ifInErrors;
        private final Counter32 _ifInNUcastPkts;
        private final Counter32 _ifInOctets;
        private final Counter32 _ifInUcastPkts;
        private final Counter32 _ifInUnknownProtos;
        private final InterfaceIndex _ifIndex;
        private final Timeticks _ifLastChange;
        private final java.lang.Integer _ifMtu;
        private final IfOperStatus _ifOperStatus;
        private final Counter32 _ifOutDiscards;
        private final Counter32 _ifOutErrors;
        private final Counter32 _ifOutNUcastPkts;
        private final Counter32 _ifOutOctets;
        private final Gauge32 _ifOutQLen;
        private final Counter32 _ifOutUcastPkts;
        private final PhysAddress _ifPhysAddress;
        private final ObjectIdentifier _ifSpecific;
        private final Gauge32 _ifSpeed;
        private final IANAifType _ifType;
        private final IfEntryKey _key;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>> augmentation = new HashMap<>();

        private IfEntryImpl(IfEntryBuilder base) {
            if (base.getKey() == null) {
                this._key = new IfEntryKey(
                        base.getIfIndex()
                );
                this._ifIndex = base.getIfIndex();
            } else {
                this._key = base.getKey();
                this._ifIndex = _key.getIfIndex();
            }
            this._ifAdminStatus = base.getIfAdminStatus();
            this._ifDescr = base.getIfDescr();
            this._ifInDiscards = base.getIfInDiscards();
            this._ifInErrors = base.getIfInErrors();
            this._ifInNUcastPkts = base.getIfInNUcastPkts();
            this._ifInOctets = base.getIfInOctets();
            this._ifInUcastPkts = base.getIfInUcastPkts();
            this._ifInUnknownProtos = base.getIfInUnknownProtos();
            this._ifLastChange = base.getIfLastChange();
            this._ifMtu = base.getIfMtu();
            this._ifOperStatus = base.getIfOperStatus();
            this._ifOutDiscards = base.getIfOutDiscards();
            this._ifOutErrors = base.getIfOutErrors();
            this._ifOutNUcastPkts = base.getIfOutNUcastPkts();
            this._ifOutOctets = base.getIfOutOctets();
            this._ifOutQLen = base.getIfOutQLen();
            this._ifOutUcastPkts = base.getIfOutUcastPkts();
            this._ifPhysAddress = base.getIfPhysAddress();
            this._ifSpecific = base.getIfSpecific();
            this._ifSpeed = base.getIfSpeed();
            this._ifType = base.getIfType();
            switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                case 1:
                    final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>> e = base.augmentation.entrySet().iterator().next();
                    this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>>singletonMap(e.getKey(), e.getValue());
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public IfAdminStatus getIfAdminStatus() {
            return _ifAdminStatus;
        }

        @Override
        public java.lang.String getIfDescr() {
            return _ifDescr;
        }

        @Override
        public Counter32 getIfInDiscards() {
            return _ifInDiscards;
        }

        @Override
        public Counter32 getIfInErrors() {
            return _ifInErrors;
        }

        @Override
        public Counter32 getIfInNUcastPkts() {
            return _ifInNUcastPkts;
        }

        @Override
        public Counter32 getIfInOctets() {
            return _ifInOctets;
        }

        @Override
        public Counter32 getIfInUcastPkts() {
            return _ifInUcastPkts;
        }

        @Override
        public Counter32 getIfInUnknownProtos() {
            return _ifInUnknownProtos;
        }

        @Override
        public InterfaceIndex getIfIndex() {
            return _ifIndex;
        }

        @Override
        public Timeticks getIfLastChange() {
            return _ifLastChange;
        }

        @Override
        public java.lang.Integer getIfMtu() {
            return _ifMtu;
        }

        @Override
        public IfOperStatus getIfOperStatus() {
            return _ifOperStatus;
        }

        @Override
        public Counter32 getIfOutDiscards() {
            return _ifOutDiscards;
        }

        @Override
        public Counter32 getIfOutErrors() {
            return _ifOutErrors;
        }

        @Override
        public Counter32 getIfOutNUcastPkts() {
            return _ifOutNUcastPkts;
        }

        @Override
        public Counter32 getIfOutOctets() {
            return _ifOutOctets;
        }

        @Override
        public Gauge32 getIfOutQLen() {
            return _ifOutQLen;
        }

        @Override
        public Counter32 getIfOutUcastPkts() {
            return _ifOutUcastPkts;
        }

        @Override
        public PhysAddress getIfPhysAddress() {
            return _ifPhysAddress;
        }

        @Override
        public ObjectIdentifier getIfSpecific() {
            return _ifSpecific;
        }

        @Override
        public Gauge32 getIfSpeed() {
            return _ifSpeed;
        }

        @Override
        public IANAifType getIfType() {
            return _ifType;
        }

        @Override
        public IfEntryKey getKey() {
            return _key;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_ifAdminStatus == null) ? 0 : _ifAdminStatus.hashCode());
            result = prime * result + ((_ifDescr == null) ? 0 : _ifDescr.hashCode());
            result = prime * result + ((_ifInDiscards == null) ? 0 : _ifInDiscards.hashCode());
            result = prime * result + ((_ifInErrors == null) ? 0 : _ifInErrors.hashCode());
            result = prime * result + ((_ifInNUcastPkts == null) ? 0 : _ifInNUcastPkts.hashCode());
            result = prime * result + ((_ifInOctets == null) ? 0 : _ifInOctets.hashCode());
            result = prime * result + ((_ifInUcastPkts == null) ? 0 : _ifInUcastPkts.hashCode());
            result = prime * result + ((_ifInUnknownProtos == null) ? 0 : _ifInUnknownProtos.hashCode());
            result = prime * result + ((_ifIndex == null) ? 0 : _ifIndex.hashCode());
            result = prime * result + ((_ifLastChange == null) ? 0 : _ifLastChange.hashCode());
            result = prime * result + ((_ifMtu == null) ? 0 : _ifMtu.hashCode());
            result = prime * result + ((_ifOperStatus == null) ? 0 : _ifOperStatus.hashCode());
            result = prime * result + ((_ifOutDiscards == null) ? 0 : _ifOutDiscards.hashCode());
            result = prime * result + ((_ifOutErrors == null) ? 0 : _ifOutErrors.hashCode());
            result = prime * result + ((_ifOutNUcastPkts == null) ? 0 : _ifOutNUcastPkts.hashCode());
            result = prime * result + ((_ifOutOctets == null) ? 0 : _ifOutOctets.hashCode());
            result = prime * result + ((_ifOutQLen == null) ? 0 : _ifOutQLen.hashCode());
            result = prime * result + ((_ifOutUcastPkts == null) ? 0 : _ifOutUcastPkts.hashCode());
            result = prime * result + ((_ifPhysAddress == null) ? 0 : _ifPhysAddress.hashCode());
            result = prime * result + ((_ifSpecific == null) ? 0 : _ifSpecific.hashCode());
            result = prime * result + ((_ifSpeed == null) ? 0 : _ifSpeed.hashCode());
            result = prime * result + ((_ifType == null) ? 0 : _ifType.hashCode());
            result = prime * result + ((_key == null) ? 0 : _key.hashCode());
            result = prime * result + ((augmentation == null) ? 0 : augmentation.hashCode());
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry other = (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry)obj;
            if (_ifAdminStatus == null) {
                if (other.getIfAdminStatus() != null) {
                    return false;
                }
            } else if(!_ifAdminStatus.equals(other.getIfAdminStatus())) {
                return false;
            }
            if (_ifDescr == null) {
                if (other.getIfDescr() != null) {
                    return false;
                }
            } else if(!_ifDescr.equals(other.getIfDescr())) {
                return false;
            }
            if (_ifInDiscards == null) {
                if (other.getIfInDiscards() != null) {
                    return false;
                }
            } else if(!_ifInDiscards.equals(other.getIfInDiscards())) {
                return false;
            }
            if (_ifInErrors == null) {
                if (other.getIfInErrors() != null) {
                    return false;
                }
            } else if(!_ifInErrors.equals(other.getIfInErrors())) {
                return false;
            }
            if (_ifInNUcastPkts == null) {
                if (other.getIfInNUcastPkts() != null) {
                    return false;
                }
            } else if(!_ifInNUcastPkts.equals(other.getIfInNUcastPkts())) {
                return false;
            }
            if (_ifInOctets == null) {
                if (other.getIfInOctets() != null) {
                    return false;
                }
            } else if(!_ifInOctets.equals(other.getIfInOctets())) {
                return false;
            }
            if (_ifInUcastPkts == null) {
                if (other.getIfInUcastPkts() != null) {
                    return false;
                }
            } else if(!_ifInUcastPkts.equals(other.getIfInUcastPkts())) {
                return false;
            }
            if (_ifInUnknownProtos == null) {
                if (other.getIfInUnknownProtos() != null) {
                    return false;
                }
            } else if(!_ifInUnknownProtos.equals(other.getIfInUnknownProtos())) {
                return false;
            }
            if (_ifIndex == null) {
                if (other.getIfIndex() != null) {
                    return false;
                }
            } else if(!_ifIndex.equals(other.getIfIndex())) {
                return false;
            }
            if (_ifLastChange == null) {
                if (other.getIfLastChange() != null) {
                    return false;
                }
            } else if(!_ifLastChange.equals(other.getIfLastChange())) {
                return false;
            }
            if (_ifMtu == null) {
                if (other.getIfMtu() != null) {
                    return false;
                }
            } else if(!_ifMtu.equals(other.getIfMtu())) {
                return false;
            }
            if (_ifOperStatus == null) {
                if (other.getIfOperStatus() != null) {
                    return false;
                }
            } else if(!_ifOperStatus.equals(other.getIfOperStatus())) {
                return false;
            }
            if (_ifOutDiscards == null) {
                if (other.getIfOutDiscards() != null) {
                    return false;
                }
            } else if(!_ifOutDiscards.equals(other.getIfOutDiscards())) {
                return false;
            }
            if (_ifOutErrors == null) {
                if (other.getIfOutErrors() != null) {
                    return false;
                }
            } else if(!_ifOutErrors.equals(other.getIfOutErrors())) {
                return false;
            }
            if (_ifOutNUcastPkts == null) {
                if (other.getIfOutNUcastPkts() != null) {
                    return false;
                }
            } else if(!_ifOutNUcastPkts.equals(other.getIfOutNUcastPkts())) {
                return false;
            }
            if (_ifOutOctets == null) {
                if (other.getIfOutOctets() != null) {
                    return false;
                }
            } else if(!_ifOutOctets.equals(other.getIfOutOctets())) {
                return false;
            }
            if (_ifOutQLen == null) {
                if (other.getIfOutQLen() != null) {
                    return false;
                }
            } else if(!_ifOutQLen.equals(other.getIfOutQLen())) {
                return false;
            }
            if (_ifOutUcastPkts == null) {
                if (other.getIfOutUcastPkts() != null) {
                    return false;
                }
            } else if(!_ifOutUcastPkts.equals(other.getIfOutUcastPkts())) {
                return false;
            }
            if (_ifPhysAddress == null) {
                if (other.getIfPhysAddress() != null) {
                    return false;
                }
            } else if(!_ifPhysAddress.equals(other.getIfPhysAddress())) {
                return false;
            }
            if (_ifSpecific == null) {
                if (other.getIfSpecific() != null) {
                    return false;
                }
            } else if(!_ifSpecific.equals(other.getIfSpecific())) {
                return false;
            }
            if (_ifSpeed == null) {
                if (other.getIfSpeed() != null) {
                    return false;
                }
            } else if(!_ifSpeed.equals(other.getIfSpeed())) {
                return false;
            }
            if (_ifType == null) {
                if (other.getIfType() != null) {
                    return false;
                }
            } else if(!_ifType.equals(other.getIfType())) {
                return false;
            }
            if (_key == null) {
                if (other.getKey() != null) {
                    return false;
                }
            } else if(!_key.equals(other.getKey())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                IfEntryImpl otherImpl = (IfEntryImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>>, Augmentation<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("IfEntry [");
            boolean first = true;

            if (_ifAdminStatus != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifAdminStatus=");
                builder.append(_ifAdminStatus);
            }
            if (_ifDescr != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifDescr=");
                builder.append(_ifDescr);
            }
            if (_ifInDiscards != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifInDiscards=");
                builder.append(_ifInDiscards);
            }
            if (_ifInErrors != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifInErrors=");
                builder.append(_ifInErrors);
            }
            if (_ifInNUcastPkts != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifInNUcastPkts=");
                builder.append(_ifInNUcastPkts);
            }
            if (_ifInOctets != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifInOctets=");
                builder.append(_ifInOctets);
            }
            if (_ifInUcastPkts != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifInUcastPkts=");
                builder.append(_ifInUcastPkts);
            }
            if (_ifInUnknownProtos != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifInUnknownProtos=");
                builder.append(_ifInUnknownProtos);
            }
            if (_ifIndex != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifIndex=");
                builder.append(_ifIndex);
            }
            if (_ifLastChange != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifLastChange=");
                builder.append(_ifLastChange);
            }
            if (_ifMtu != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifMtu=");
                builder.append(_ifMtu);
            }
            if (_ifOperStatus != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifOperStatus=");
                builder.append(_ifOperStatus);
            }
            if (_ifOutDiscards != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifOutDiscards=");
                builder.append(_ifOutDiscards);
            }
            if (_ifOutErrors != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifOutErrors=");
                builder.append(_ifOutErrors);
            }
            if (_ifOutNUcastPkts != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifOutNUcastPkts=");
                builder.append(_ifOutNUcastPkts);
            }
            if (_ifOutOctets != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifOutOctets=");
                builder.append(_ifOutOctets);
            }
            if (_ifOutQLen != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifOutQLen=");
                builder.append(_ifOutQLen);
            }
            if (_ifOutUcastPkts != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifOutUcastPkts=");
                builder.append(_ifOutUcastPkts);
            }
            if (_ifPhysAddress != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifPhysAddress=");
                builder.append(_ifPhysAddress);
            }
            if (_ifSpecific != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifSpecific=");
                builder.append(_ifSpecific);
            }
            if (_ifSpeed != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifSpeed=");
                builder.append(_ifSpeed);
            }
            if (_ifType != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ifType=");
                builder.append(_ifType);
            }
            if (_key != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_key=");
                builder.append(_key);
            }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
