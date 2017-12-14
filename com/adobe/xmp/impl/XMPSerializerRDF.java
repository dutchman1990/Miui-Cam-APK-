package com.adobe.xmp.impl;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.SerializeOptions;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class XMPSerializerRDF {
    static final Set RDF_ATTR_QUALIFIER = new HashSet(Arrays.asList(new String[]{"xml:lang", "rdf:resource", "rdf:ID", "rdf:bagID", "rdf:nodeID"}));
    private SerializeOptions options;
    private CountOutputStream outputStream;
    private int padding;
    private int unicodeSize = 1;
    private OutputStreamWriter writer;
    private XMPMetaImpl xmp;

    private void addPadding(int i) throws XMPException, IOException {
        if (this.options.getExactPacketLength()) {
            int bytesWritten = this.outputStream.getBytesWritten() + (this.unicodeSize * i);
            if (bytesWritten > this.padding) {
                throw new XMPException("Can't fit into specified packet size", 107);
            }
            this.padding -= bytesWritten;
        }
        this.padding /= this.unicodeSize;
        int length = this.options.getNewline().length();
        if (this.padding >= length) {
            this.padding -= length;
            while (this.padding >= length + 100) {
                writeChars(100, ' ');
                writeNewline();
                this.padding -= length + 100;
            }
            writeChars(this.padding, ' ');
            writeNewline();
            return;
        }
        writeChars(this.padding, ' ');
    }

    private void appendNodeValue(String str, boolean z) throws IOException {
        write(Utils.escapeXML(str, z, true));
    }

    private boolean canBeRDFAttrProp(XMPNode xMPNode) {
        return (xMPNode.hasQualifier() || xMPNode.getOptions().isURI() || xMPNode.getOptions().isCompositeProperty() || "[]".equals(xMPNode.getName())) ? false : true;
    }

    private void declareNamespace(String str, String str2, Set set, int i) throws IOException {
        if (str2 == null) {
            QName qName = new QName(str);
            if (qName.hasPrefix()) {
                str = qName.getPrefix();
                str2 = XMPMetaFactory.getSchemaRegistry().getNamespaceURI(str + ":");
                declareNamespace(str, str2, set, i);
            } else {
                return;
            }
        }
        if (!set.contains(str)) {
            writeNewline();
            writeIndent(i);
            write("xmlns:");
            write(str);
            write("=\"");
            write(str2);
            write(34);
            set.add(str);
        }
    }

    private void declareUsedNamespaces(XMPNode xMPNode, Set set, int i) throws IOException {
        Iterator iterateChildren;
        if (xMPNode.getOptions().isSchemaNode()) {
            declareNamespace(xMPNode.getValue().substring(0, xMPNode.getValue().length() - 1), xMPNode.getName(), set, i);
        } else if (xMPNode.getOptions().isStruct()) {
            iterateChildren = xMPNode.iterateChildren();
            while (iterateChildren.hasNext()) {
                declareNamespace(((XMPNode) iterateChildren.next()).getName(), null, set, i);
            }
        }
        iterateChildren = xMPNode.iterateChildren();
        while (iterateChildren.hasNext()) {
            declareUsedNamespaces((XMPNode) iterateChildren.next(), set, i);
        }
        iterateChildren = xMPNode.iterateQualifier();
        while (iterateChildren.hasNext()) {
            XMPNode xMPNode2 = (XMPNode) iterateChildren.next();
            declareNamespace(xMPNode2.getName(), null, set, i);
            declareUsedNamespaces(xMPNode2, set, i);
        }
    }

    private void emitRDFArrayTag(XMPNode xMPNode, boolean z, int i) throws IOException {
        if (z || xMPNode.hasChildren()) {
            writeIndent(i);
            write(z ? "<rdf:" : "</rdf:");
            if (xMPNode.getOptions().isArrayAlternate()) {
                write("Alt");
            } else if (xMPNode.getOptions().isArrayOrdered()) {
                write("Seq");
            } else {
                write("Bag");
            }
            if (!z || xMPNode.hasChildren()) {
                write(">");
            } else {
                write("/>");
            }
            writeNewline();
        }
    }

    private String serializeAsRDF() throws IOException, XMPException {
        if (!this.options.getOmitPacketWrapper()) {
            writeIndent(0);
            write("<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>");
            writeNewline();
        }
        writeIndent(0);
        write("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"");
        if (!this.options.getOmitVersionAttribute()) {
            write(XMPMetaFactory.getVersionInfo().getMessage());
        }
        write("\">");
        writeNewline();
        writeIndent(1);
        write("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">");
        writeNewline();
        if (this.options.getUseCompactFormat()) {
            serializeCompactRDFSchemas();
        } else {
            serializePrettyRDFSchemas();
        }
        writeIndent(1);
        write("</rdf:RDF>");
        writeNewline();
        writeIndent(0);
        write("</x:xmpmeta>");
        writeNewline();
        String str = "";
        if (this.options.getOmitPacketWrapper()) {
            return str;
        }
        for (int baseIndent = this.options.getBaseIndent(); baseIndent > 0; baseIndent--) {
            str = str + this.options.getIndent();
        }
        return ((str + "<?xpacket end=\"") + (this.options.getReadOnlyPacket() ? 'r' : 'w')) + "\"?>";
    }

    private void serializeCompactRDFArrayProp(XMPNode xMPNode, int i) throws IOException, XMPException {
        write(62);
        writeNewline();
        emitRDFArrayTag(xMPNode, true, i + 1);
        if (xMPNode.getOptions().isArrayAltText()) {
            XMPNodeUtils.normalizeLangArray(xMPNode);
        }
        serializeCompactRDFElementProps(xMPNode, i + 2);
        emitRDFArrayTag(xMPNode, false, i + 1);
    }

    private boolean serializeCompactRDFAttrProps(XMPNode xMPNode, int i) throws IOException {
        boolean z = true;
        Iterator iterateChildren = xMPNode.iterateChildren();
        while (iterateChildren.hasNext()) {
            XMPNode xMPNode2 = (XMPNode) iterateChildren.next();
            if (canBeRDFAttrProp(xMPNode2)) {
                writeNewline();
                writeIndent(i);
                write(xMPNode2.getName());
                write("=\"");
                appendNodeValue(xMPNode2.getValue(), true);
                write(34);
            } else {
                z = false;
            }
        }
        return z;
    }

    private void serializeCompactRDFElementProps(XMPNode xMPNode, int i) throws IOException, XMPException {
        Iterator iterateChildren = xMPNode.iterateChildren();
        while (iterateChildren.hasNext()) {
            XMPNode xMPNode2 = (XMPNode) iterateChildren.next();
            if (!canBeRDFAttrProp(xMPNode2)) {
                boolean z = true;
                boolean z2 = true;
                String name = xMPNode2.getName();
                if ("[]".equals(name)) {
                    name = "rdf:li";
                }
                writeIndent(i);
                write(60);
                write(name);
                Object obj = null;
                boolean z3 = false;
                Iterator iterateQualifier = xMPNode2.iterateQualifier();
                while (iterateQualifier.hasNext()) {
                    XMPNode xMPNode3 = (XMPNode) iterateQualifier.next();
                    if (RDF_ATTR_QUALIFIER.contains(xMPNode3.getName())) {
                        z3 = "rdf:resource".equals(xMPNode3.getName());
                        write(32);
                        write(xMPNode3.getName());
                        write("=\"");
                        appendNodeValue(xMPNode3.getValue(), true);
                        write(34);
                    } else {
                        obj = 1;
                    }
                }
                if (obj != null) {
                    serializeCompactRDFGeneralQualifier(i, xMPNode2);
                } else if (!xMPNode2.getOptions().isCompositeProperty()) {
                    Object[] serializeCompactRDFSimpleProp = serializeCompactRDFSimpleProp(xMPNode2);
                    z = ((Boolean) serializeCompactRDFSimpleProp[0]).booleanValue();
                    z2 = ((Boolean) serializeCompactRDFSimpleProp[1]).booleanValue();
                } else if (xMPNode2.getOptions().isArray()) {
                    serializeCompactRDFArrayProp(xMPNode2, i);
                } else {
                    z = serializeCompactRDFStructProp(xMPNode2, i, z3);
                }
                if (z) {
                    if (z2) {
                        writeIndent(i);
                    }
                    write("</");
                    write(name);
                    write(62);
                    writeNewline();
                }
            }
        }
    }

    private void serializeCompactRDFGeneralQualifier(int i, XMPNode xMPNode) throws IOException, XMPException {
        write(" rdf:parseType=\"Resource\">");
        writeNewline();
        serializePrettyRDFProperty(xMPNode, true, i + 1);
        Iterator iterateQualifier = xMPNode.iterateQualifier();
        while (iterateQualifier.hasNext()) {
            serializePrettyRDFProperty((XMPNode) iterateQualifier.next(), false, i + 1);
        }
    }

    private void serializeCompactRDFSchemas() throws IOException, XMPException {
        writeIndent(2);
        write("<rdf:Description rdf:about=");
        writeTreeName();
        Set hashSet = new HashSet();
        hashSet.add("xml");
        hashSet.add("rdf");
        Iterator iterateChildren = this.xmp.getRoot().iterateChildren();
        while (iterateChildren.hasNext()) {
            declareUsedNamespaces((XMPNode) iterateChildren.next(), hashSet, 4);
        }
        int i = 1;
        iterateChildren = this.xmp.getRoot().iterateChildren();
        while (iterateChildren.hasNext()) {
            i &= serializeCompactRDFAttrProps((XMPNode) iterateChildren.next(), 3);
        }
        if (i == 0) {
            write(62);
            writeNewline();
            iterateChildren = this.xmp.getRoot().iterateChildren();
            while (iterateChildren.hasNext()) {
                serializeCompactRDFElementProps((XMPNode) iterateChildren.next(), 3);
            }
            writeIndent(2);
            write("</rdf:Description>");
            writeNewline();
            return;
        }
        write("/>");
        writeNewline();
    }

    private Object[] serializeCompactRDFSimpleProp(XMPNode xMPNode) throws IOException {
        Boolean bool = Boolean.TRUE;
        Boolean bool2 = Boolean.TRUE;
        if (xMPNode.getOptions().isURI()) {
            write(" rdf:resource=\"");
            appendNodeValue(xMPNode.getValue(), true);
            write("\"/>");
            writeNewline();
            bool = Boolean.FALSE;
        } else if (xMPNode.getValue() == null || xMPNode.getValue().length() == 0) {
            write("/>");
            writeNewline();
            bool = Boolean.FALSE;
        } else {
            write(62);
            appendNodeValue(xMPNode.getValue(), false);
            bool2 = Boolean.FALSE;
        }
        return new Object[]{bool, bool2};
    }

    private boolean serializeCompactRDFStructProp(XMPNode xMPNode, int i, boolean z) throws XMPException, IOException {
        Object obj = null;
        Object obj2 = null;
        Iterator iterateChildren = xMPNode.iterateChildren();
        while (iterateChildren.hasNext()) {
            if (canBeRDFAttrProp((XMPNode) iterateChildren.next())) {
                obj = 1;
            } else {
                obj2 = 1;
            }
            if (obj != null && r3 != null) {
                break;
            }
        }
        if (z && obj2 != null) {
            throw new XMPException("Can't mix rdf:resource qualifier and element fields", 202);
        } else if (!xMPNode.hasChildren()) {
            write(" rdf:parseType=\"Resource\"/>");
            writeNewline();
            return false;
        } else if (obj2 == null) {
            serializeCompactRDFAttrProps(xMPNode, i + 1);
            write("/>");
            writeNewline();
            return false;
        } else if (obj == null) {
            write(" rdf:parseType=\"Resource\">");
            writeNewline();
            serializeCompactRDFElementProps(xMPNode, i + 1);
            return true;
        } else {
            write(62);
            writeNewline();
            writeIndent(i + 1);
            write("<rdf:Description");
            serializeCompactRDFAttrProps(xMPNode, i + 2);
            write(">");
            writeNewline();
            serializeCompactRDFElementProps(xMPNode, i + 1);
            writeIndent(i + 1);
            write("</rdf:Description>");
            writeNewline();
            return true;
        }
    }

    private void serializePrettyRDFProperty(XMPNode xMPNode, boolean z, int i) throws IOException, XMPException {
        Object obj = 1;
        Object obj2 = 1;
        String name = xMPNode.getName();
        if (z) {
            name = "rdf:value";
        } else if ("[]".equals(name)) {
            name = "rdf:li";
        }
        writeIndent(i);
        write(60);
        write(name);
        Object obj3 = null;
        boolean z2 = false;
        Iterator iterateQualifier = xMPNode.iterateQualifier();
        while (iterateQualifier.hasNext()) {
            XMPNode xMPNode2 = (XMPNode) iterateQualifier.next();
            if (RDF_ATTR_QUALIFIER.contains(xMPNode2.getName())) {
                z2 = "rdf:resource".equals(xMPNode2.getName());
                if (!z) {
                    write(32);
                    write(xMPNode2.getName());
                    write("=\"");
                    appendNodeValue(xMPNode2.getValue(), true);
                    write(34);
                }
            } else {
                obj3 = 1;
            }
        }
        if (obj3 == null || z) {
            if (xMPNode.getOptions().isCompositeProperty()) {
                if (xMPNode.getOptions().isArray()) {
                    write(62);
                    writeNewline();
                    emitRDFArrayTag(xMPNode, true, i + 1);
                    if (xMPNode.getOptions().isArrayAltText()) {
                        XMPNodeUtils.normalizeLangArray(xMPNode);
                    }
                    iterateQualifier = xMPNode.iterateChildren();
                    while (iterateQualifier.hasNext()) {
                        serializePrettyRDFProperty((XMPNode) iterateQualifier.next(), false, i + 2);
                    }
                    emitRDFArrayTag(xMPNode, false, i + 1);
                } else if (z2) {
                    iterateQualifier = xMPNode.iterateChildren();
                    while (iterateQualifier.hasNext()) {
                        XMPNode xMPNode3 = (XMPNode) iterateQualifier.next();
                        if (canBeRDFAttrProp(xMPNode3)) {
                            writeNewline();
                            writeIndent(i + 1);
                            write(32);
                            write(xMPNode3.getName());
                            write("=\"");
                            appendNodeValue(xMPNode3.getValue(), true);
                            write(34);
                        } else {
                            throw new XMPException("Can't mix rdf:resource and complex fields", 202);
                        }
                    }
                    write("/>");
                    writeNewline();
                    obj = null;
                } else if (xMPNode.hasChildren()) {
                    write(" rdf:parseType=\"Resource\">");
                    writeNewline();
                    iterateQualifier = xMPNode.iterateChildren();
                    while (iterateQualifier.hasNext()) {
                        serializePrettyRDFProperty((XMPNode) iterateQualifier.next(), false, i + 1);
                    }
                } else {
                    write(" rdf:parseType=\"Resource\"/>");
                    writeNewline();
                    obj = null;
                }
            } else if (xMPNode.getOptions().isURI()) {
                write(" rdf:resource=\"");
                appendNodeValue(xMPNode.getValue(), true);
                write("\"/>");
                writeNewline();
                obj = null;
            } else if (xMPNode.getValue() == null || "".equals(xMPNode.getValue())) {
                write("/>");
                writeNewline();
                obj = null;
            } else {
                write(62);
                appendNodeValue(xMPNode.getValue(), false);
                obj2 = null;
            }
        } else if (z2) {
            throw new XMPException("Can't mix rdf:resource and general qualifiers", 202);
        } else {
            write(" rdf:parseType=\"Resource\">");
            writeNewline();
            serializePrettyRDFProperty(xMPNode, true, i + 1);
            iterateQualifier = xMPNode.iterateQualifier();
            while (iterateQualifier.hasNext()) {
                xMPNode2 = (XMPNode) iterateQualifier.next();
                if (!RDF_ATTR_QUALIFIER.contains(xMPNode2.getName())) {
                    serializePrettyRDFProperty(xMPNode2, false, i + 1);
                }
            }
        }
        if (obj != null) {
            if (obj2 != null) {
                writeIndent(i);
            }
            write("</");
            write(name);
            write(62);
            writeNewline();
        }
    }

    private void serializePrettyRDFSchema(XMPNode xMPNode) throws IOException, XMPException {
        writeIndent(2);
        write("<rdf:Description rdf:about=");
        writeTreeName();
        Set hashSet = new HashSet();
        hashSet.add("xml");
        hashSet.add("rdf");
        declareUsedNamespaces(xMPNode, hashSet, 4);
        write(62);
        writeNewline();
        Iterator iterateChildren = xMPNode.iterateChildren();
        while (iterateChildren.hasNext()) {
            serializePrettyRDFProperty((XMPNode) iterateChildren.next(), false, 3);
        }
        writeIndent(2);
        write("</rdf:Description>");
        writeNewline();
    }

    private void serializePrettyRDFSchemas() throws IOException, XMPException {
        if (this.xmp.getRoot().getChildrenLength() > 0) {
            Iterator iterateChildren = this.xmp.getRoot().iterateChildren();
            while (iterateChildren.hasNext()) {
                serializePrettyRDFSchema((XMPNode) iterateChildren.next());
            }
            return;
        }
        writeIndent(2);
        write("<rdf:Description rdf:about=");
        writeTreeName();
        write("/>");
        writeNewline();
    }

    private void write(int i) throws IOException {
        this.writer.write(i);
    }

    private void write(String str) throws IOException {
        this.writer.write(str);
    }

    private void writeChars(int i, char c) throws IOException {
        while (i > 0) {
            this.writer.write(c);
            i--;
        }
    }

    private void writeIndent(int i) throws IOException {
        for (int baseIndent = this.options.getBaseIndent() + i; baseIndent > 0; baseIndent--) {
            this.writer.write(this.options.getIndent());
        }
    }

    private void writeNewline() throws IOException {
        this.writer.write(this.options.getNewline());
    }

    private void writeTreeName() throws IOException {
        write(34);
        String name = this.xmp.getRoot().getName();
        if (name != null) {
            appendNodeValue(name, true);
        }
        write(34);
    }

    protected void checkOptionsConsistence() throws XMPException {
        if ((this.options.getEncodeUTF16BE() | this.options.getEncodeUTF16LE()) != 0) {
            this.unicodeSize = 2;
        }
        if (this.options.getExactPacketLength()) {
            if ((this.options.getOmitPacketWrapper() | this.options.getIncludeThumbnailPad()) != 0) {
                throw new XMPException("Inconsistent options for exact size serialize", 103);
            } else if ((this.options.getPadding() & (this.unicodeSize - 1)) != 0) {
                throw new XMPException("Exact size must be a multiple of the Unicode element", 103);
            }
        } else if (this.options.getReadOnlyPacket()) {
            if ((this.options.getOmitPacketWrapper() | this.options.getIncludeThumbnailPad()) != 0) {
                throw new XMPException("Inconsistent options for read-only packet", 103);
            }
            this.padding = 0;
        } else if (!this.options.getOmitPacketWrapper()) {
            if (this.padding == 0) {
                this.padding = this.unicodeSize * 2048;
            }
            if (this.options.getIncludeThumbnailPad() && !this.xmp.doesPropertyExist("http://ns.adobe.com/xap/1.0/", "Thumbnails")) {
                this.padding += this.unicodeSize * 10000;
            }
        } else if (this.options.getIncludeThumbnailPad()) {
            throw new XMPException("Inconsistent options for non-packet serialize", 103);
        } else {
            this.padding = 0;
        }
    }

    public void serialize(XMPMeta xMPMeta, OutputStream outputStream, SerializeOptions serializeOptions) throws XMPException {
        try {
            this.outputStream = new CountOutputStream(outputStream);
            this.writer = new OutputStreamWriter(this.outputStream, serializeOptions.getEncoding());
            this.xmp = (XMPMetaImpl) xMPMeta;
            this.options = serializeOptions;
            this.padding = serializeOptions.getPadding();
            this.writer = new OutputStreamWriter(this.outputStream, serializeOptions.getEncoding());
            checkOptionsConsistence();
            String serializeAsRDF = serializeAsRDF();
            this.writer.flush();
            addPadding(serializeAsRDF.length());
            write(serializeAsRDF);
            this.writer.flush();
            this.outputStream.close();
        } catch (IOException e) {
            throw new XMPException("Error writing to the OutputStream", 0);
        }
    }
}
