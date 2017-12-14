package com.adobe.xmp.impl;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.options.PropertyOptions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ParseRDF {
    static final /* synthetic */ boolean -assertionsDisabled = (!ParseRDF.class.desiredAssertionStatus());

    private static XMPNode addChildNode(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, String str, boolean z) throws XMPException {
        XMPSchemaRegistry schemaRegistry = XMPMetaFactory.getSchemaRegistry();
        String namespaceURI = node.getNamespaceURI();
        if (namespaceURI != null) {
            if ("http://purl.org/dc/1.1/".equals(namespaceURI)) {
                namespaceURI = "http://purl.org/dc/elements/1.1/";
            }
            String namespacePrefix = schemaRegistry.getNamespacePrefix(namespaceURI);
            if (namespacePrefix == null) {
                namespacePrefix = schemaRegistry.registerNamespace(namespaceURI, node.getPrefix() != null ? node.getPrefix() : "_dflt");
            }
            String str2 = namespacePrefix + node.getLocalName();
            PropertyOptions propertyOptions = new PropertyOptions();
            boolean z2 = false;
            if (z) {
                XMPNode findSchemaNode = XMPNodeUtils.findSchemaNode(xMPMetaImpl.getRoot(), namespaceURI, "_dflt", true);
                findSchemaNode.setImplicit(false);
                xMPNode = findSchemaNode;
                if (schemaRegistry.findAlias(str2) != null) {
                    z2 = true;
                    xMPMetaImpl.getRoot().setHasAliases(true);
                    findSchemaNode.setHasAliases(true);
                }
            }
            boolean equals = "rdf:li".equals(str2);
            boolean equals2 = "rdf:value".equals(str2);
            XMPNode xMPNode2 = new XMPNode(str2, str, propertyOptions);
            xMPNode2.setAlias(z2);
            if (equals2) {
                xMPNode.addChild(1, xMPNode2);
            } else {
                xMPNode.addChild(xMPNode2);
            }
            if (equals2) {
                if (z || !xMPNode.getOptions().isStruct()) {
                    throw new XMPException("Misplaced rdf:value element", 202);
                }
                xMPNode.setHasValueChild(true);
            }
            if (equals) {
                if (xMPNode.getOptions().isArray()) {
                    xMPNode2.setName("[]");
                } else {
                    throw new XMPException("Misplaced rdf:li element", 202);
                }
            }
            return xMPNode2;
        }
        throw new XMPException("XML namespace required for all elements and attributes", 202);
    }

    private static XMPNode addQualifierNode(XMPNode xMPNode, String str, String str2) throws XMPException {
        if ("xml:lang".equals(str)) {
            str2 = Utils.normalizeLangValue(str2);
        }
        XMPNode xMPNode2 = new XMPNode(str, str2, null);
        xMPNode.addQualifier(xMPNode2);
        return xMPNode2;
    }

    private static void fixupQualifiedNode(XMPNode xMPNode) throws XMPException {
        int i = 1;
        if (!-assertionsDisabled) {
            if (!(xMPNode.getOptions().isStruct() ? xMPNode.hasChildren() : false)) {
                throw new AssertionError();
            }
        }
        XMPNode child = xMPNode.getChild(1);
        if (-assertionsDisabled || "rdf:value".equals(child.getName())) {
            int i2;
            if (child.getOptions().getHasLanguage()) {
                if (xMPNode.getOptions().getHasLanguage()) {
                    throw new XMPException("Redundant xml:lang for rdf:value element", 203);
                }
                XMPNode qualifier = child.getQualifier(1);
                child.removeQualifier(qualifier);
                xMPNode.addQualifier(qualifier);
            }
            for (i2 = 1; i2 <= child.getQualifierLength(); i2++) {
                xMPNode.addQualifier(child.getQualifier(i2));
            }
            for (i2 = 2; i2 <= xMPNode.getChildrenLength(); i2++) {
                xMPNode.addQualifier(xMPNode.getChild(i2));
            }
            if (!-assertionsDisabled) {
                if (!xMPNode.getOptions().isStruct()) {
                    i = xMPNode.getHasValueChild();
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            xMPNode.setHasValueChild(false);
            xMPNode.getOptions().setStruct(false);
            xMPNode.getOptions().mergeWith(child.getOptions());
            xMPNode.setValue(child.getValue());
            xMPNode.removeChildren();
            Iterator iterateChildren = child.iterateChildren();
            while (iterateChildren.hasNext()) {
                xMPNode.addChild((XMPNode) iterateChildren.next());
            }
            return;
        }
        throw new AssertionError();
    }

    private static int getRDFTermKind(Node node) {
        String localName = node.getLocalName();
        Object namespaceURI = node.getNamespaceURI();
        if (namespaceURI == null && (("about".equals(localName) || "ID".equals(localName)) && (node instanceof Attr) && "http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(((Attr) node).getOwnerElement().getNamespaceURI()))) {
            namespaceURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        }
        if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceURI)) {
            if ("li".equals(localName)) {
                return 9;
            }
            if ("parseType".equals(localName)) {
                return 4;
            }
            if ("Description".equals(localName)) {
                return 8;
            }
            if ("about".equals(localName)) {
                return 3;
            }
            if ("resource".equals(localName)) {
                return 5;
            }
            if ("RDF".equals(localName)) {
                return 1;
            }
            if ("ID".equals(localName)) {
                return 2;
            }
            if ("nodeID".equals(localName)) {
                return 6;
            }
            if ("datatype".equals(localName)) {
                return 7;
            }
            if ("aboutEach".equals(localName)) {
                return 10;
            }
            if ("aboutEachPrefix".equals(localName)) {
                return 11;
            }
            if ("bagID".equals(localName)) {
                return 12;
            }
        }
        return 0;
    }

    private static boolean isCoreSyntaxTerm(int i) {
        return 1 <= i && i <= 7;
    }

    private static boolean isOldTerm(int i) {
        return 10 <= i && i <= 12;
    }

    private static boolean isPropertyElementName(int i) {
        boolean z = false;
        if (i == 8 || isOldTerm(i)) {
            return false;
        }
        if (!isCoreSyntaxTerm(i)) {
            z = true;
        }
        return z;
    }

    private static boolean isWhitespaceNode(Node node) {
        if (node.getNodeType() != (short) 3) {
            return false;
        }
        String nodeValue = node.getNodeValue();
        for (int i = 0; i < nodeValue.length(); i++) {
            if (!Character.isWhitespace(nodeValue.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static XMPMetaImpl parse(Node node) throws XMPException {
        XMPMetaImpl xMPMetaImpl = new XMPMetaImpl();
        rdf_RDF(xMPMetaImpl, node);
        return xMPMetaImpl;
    }

    private static void rdf_EmptyPropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        Object obj = null;
        Object obj2 = null;
        Object obj3 = null;
        Object obj4 = null;
        Node node2 = null;
        if (node.hasChildNodes()) {
            throw new XMPException("Nested content not allowed with rdf:resource or property attributes", 202);
        }
        int i;
        for (i = 0; i < node.getAttributes().getLength(); i++) {
            Node item = node.getAttributes().item(i);
            if (!("xmlns".equals(item.getPrefix()) || (item.getPrefix() == null && "xmlns".equals(item.getNodeName())))) {
                switch (getRDFTermKind(item)) {
                    case 0:
                        if (!"value".equals(item.getLocalName()) || !"http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(item.getNamespaceURI())) {
                            if (!"xml:lang".equals(item.getNodeName())) {
                                obj = 1;
                                break;
                            }
                            break;
                        } else if (obj2 == null) {
                            obj4 = 1;
                            node2 = item;
                            break;
                        } else {
                            throw new XMPException("Empty property element can't have both rdf:value and rdf:resource", 203);
                        }
                    case 2:
                        break;
                    case 5:
                        if (obj3 == null) {
                            if (obj4 == null) {
                                obj2 = 1;
                                if (obj4 != null) {
                                    break;
                                }
                                node2 = item;
                                break;
                            }
                            throw new XMPException("Empty property element can't have both rdf:value and rdf:resource", 203);
                        }
                        throw new XMPException("Empty property element can't have both rdf:resource and rdf:nodeID", 202);
                    case 6:
                        if (obj2 == null) {
                            obj3 = 1;
                            break;
                        }
                        throw new XMPException("Empty property element can't have both rdf:resource and rdf:nodeID", 202);
                    default:
                        throw new XMPException("Unrecognized attribute of empty property element", 202);
                }
            }
        }
        XMPNode addChildNode = addChildNode(xMPMetaImpl, xMPNode, node, "", z);
        Object obj5 = null;
        if (obj4 != null || obj2 != null) {
            addChildNode.setValue(node2 != null ? node2.getNodeValue() : "");
            if (obj4 == null) {
                addChildNode.getOptions().setURI(true);
            }
        } else if (obj != null) {
            addChildNode.getOptions().setStruct(true);
            obj5 = 1;
        }
        for (i = 0; i < node.getAttributes().getLength(); i++) {
            item = node.getAttributes().item(i);
            if (!(item == node2 || "xmlns".equals(item.getPrefix()) || (item.getPrefix() == null && "xmlns".equals(item.getNodeName())))) {
                switch (getRDFTermKind(item)) {
                    case 0:
                        if (obj5 != null) {
                            if (!"xml:lang".equals(item.getNodeName())) {
                                addChildNode(xMPMetaImpl, addChildNode, item, item.getNodeValue(), false);
                                break;
                            } else {
                                addQualifierNode(addChildNode, "xml:lang", item.getNodeValue());
                                break;
                            }
                        }
                        addQualifierNode(addChildNode, item.getNodeName(), item.getNodeValue());
                        break;
                    case 2:
                    case 6:
                        break;
                    case 5:
                        addQualifierNode(addChildNode, "rdf:resource", item.getNodeValue());
                        break;
                    default:
                        throw new XMPException("Unrecognized attribute of empty property element", 202);
                }
            }
        }
    }

    private static void rdf_LiteralPropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        int i;
        XMPNode addChildNode = addChildNode(xMPMetaImpl, xMPNode, node, null, z);
        for (i = 0; i < node.getAttributes().getLength(); i++) {
            Node item = node.getAttributes().item(i);
            if (!("xmlns".equals(item.getPrefix()) || (item.getPrefix() == null && "xmlns".equals(item.getNodeName())))) {
                String namespaceURI = item.getNamespaceURI();
                String localName = item.getLocalName();
                if ("xml:lang".equals(item.getNodeName())) {
                    addQualifierNode(addChildNode, "xml:lang", item.getNodeValue());
                } else if (!("http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceURI) && ("ID".equals(localName) || "datatype".equals(localName)))) {
                    throw new XMPException("Invalid attribute for literal property element", 202);
                }
            }
        }
        String str = "";
        i = 0;
        while (i < node.getChildNodes().getLength()) {
            Node item2 = node.getChildNodes().item(i);
            if (item2.getNodeType() == (short) 3) {
                str = str + item2.getNodeValue();
                i++;
            } else {
                throw new XMPException("Invalid child of literal property element", 202);
            }
        }
        addChildNode.setValue(str);
    }

    private static void rdf_NodeElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        int rDFTermKind = getRDFTermKind(node);
        if (rDFTermKind != 8 && rDFTermKind != 0) {
            throw new XMPException("Node element must be rdf:Description or typed node", 202);
        } else if (z && rDFTermKind == 0) {
            throw new XMPException("Top level typed node not allowed", 203);
        } else {
            rdf_NodeElementAttrs(xMPMetaImpl, xMPNode, node, z);
            rdf_PropertyElementList(xMPMetaImpl, xMPNode, node, z);
        }
    }

    private static void rdf_NodeElementAttrs(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        int i = 0;
        for (int i2 = 0; i2 < node.getAttributes().getLength(); i2++) {
            Node item = node.getAttributes().item(i2);
            if (!("xmlns".equals(item.getPrefix()) || (item.getPrefix() == null && "xmlns".equals(item.getNodeName())))) {
                int rDFTermKind = getRDFTermKind(item);
                switch (rDFTermKind) {
                    case 0:
                        addChildNode(xMPMetaImpl, xMPNode, item, item.getNodeValue(), z);
                        break;
                    case 2:
                    case 3:
                    case 6:
                        if (i <= 0) {
                            i++;
                            if (z && rDFTermKind == 3) {
                                if (xMPNode.getName() != null && xMPNode.getName().length() > 0) {
                                    if (xMPNode.getName().equals(item.getNodeValue())) {
                                        break;
                                    }
                                    throw new XMPException("Mismatched top level rdf:about values", 203);
                                }
                                xMPNode.setName(item.getNodeValue());
                                break;
                            }
                        }
                        throw new XMPException("Mutally exclusive about, ID, nodeID attributes", 202);
                        break;
                    default:
                        throw new XMPException("Invalid nodeElement attribute", 202);
                }
            }
        }
    }

    private static void rdf_NodeElementList(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node) throws XMPException {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (!isWhitespaceNode(item)) {
                rdf_NodeElement(xMPMetaImpl, xMPNode, item, true);
            }
        }
    }

    private static void rdf_ParseTypeCollectionPropertyElement() throws XMPException {
        throw new XMPException("ParseTypeCollection property element not allowed", 203);
    }

    private static void rdf_ParseTypeLiteralPropertyElement() throws XMPException {
        throw new XMPException("ParseTypeLiteral property element not allowed", 203);
    }

    private static void rdf_ParseTypeOtherPropertyElement() throws XMPException {
        throw new XMPException("ParseTypeOther property element not allowed", 203);
    }

    private static void rdf_ParseTypeResourcePropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        XMPNode addChildNode = addChildNode(xMPMetaImpl, xMPNode, node, "", z);
        addChildNode.getOptions().setStruct(true);
        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            Node item = node.getAttributes().item(i);
            if (!("xmlns".equals(item.getPrefix()) || (item.getPrefix() == null && "xmlns".equals(item.getNodeName())))) {
                String localName = item.getLocalName();
                String namespaceURI = item.getNamespaceURI();
                if ("xml:lang".equals(item.getNodeName())) {
                    addQualifierNode(addChildNode, "xml:lang", item.getNodeValue());
                } else if (!("http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceURI) && ("ID".equals(localName) || "parseType".equals(localName)))) {
                    throw new XMPException("Invalid attribute for ParseTypeResource property element", 202);
                }
            }
        }
        rdf_PropertyElementList(xMPMetaImpl, addChildNode, node, false);
        if (addChildNode.getHasValueChild()) {
            fixupQualifiedNode(addChildNode);
        }
    }

    private static void rdf_PropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        if (isPropertyElementName(getRDFTermKind(node))) {
            int i;
            Node item;
            NamedNodeMap attributes = node.getAttributes();
            List list = null;
            for (i = 0; i < attributes.getLength(); i++) {
                item = attributes.item(i);
                if ("xmlns".equals(item.getPrefix()) || (item.getPrefix() == null && "xmlns".equals(item.getNodeName()))) {
                    if (r10 == null) {
                        list = new ArrayList();
                    }
                    list.add(item.getNodeName());
                }
            }
            if (r10 != null) {
                for (String removeNamedItem : r10) {
                    attributes.removeNamedItem(removeNamedItem);
                }
            }
            if (attributes.getLength() > 3) {
                rdf_EmptyPropertyElement(xMPMetaImpl, xMPNode, node, z);
            } else {
                for (i = 0; i < attributes.getLength(); i++) {
                    item = attributes.item(i);
                    String localName = item.getLocalName();
                    String namespaceURI = item.getNamespaceURI();
                    String nodeValue = item.getNodeValue();
                    if (!"xml:lang".equals(item.getNodeName()) || ("ID".equals(localName) && "http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceURI))) {
                        if ("datatype".equals(localName) && "http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceURI)) {
                            rdf_LiteralPropertyElement(xMPMetaImpl, xMPNode, node, z);
                        } else {
                            if (!("parseType".equals(localName) ? "http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceURI) : false)) {
                                rdf_EmptyPropertyElement(xMPMetaImpl, xMPNode, node, z);
                            } else if ("Literal".equals(nodeValue)) {
                                rdf_ParseTypeLiteralPropertyElement();
                            } else if ("Resource".equals(nodeValue)) {
                                rdf_ParseTypeResourcePropertyElement(xMPMetaImpl, xMPNode, node, z);
                            } else if ("Collection".equals(nodeValue)) {
                                rdf_ParseTypeCollectionPropertyElement();
                            } else {
                                rdf_ParseTypeOtherPropertyElement();
                            }
                        }
                        return;
                    }
                }
                if (node.hasChildNodes()) {
                    for (i = 0; i < node.getChildNodes().getLength(); i++) {
                        if (node.getChildNodes().item(i).getNodeType() != (short) 3) {
                            rdf_ResourcePropertyElement(xMPMetaImpl, xMPNode, node, z);
                            return;
                        }
                    }
                    rdf_LiteralPropertyElement(xMPMetaImpl, xMPNode, node, z);
                } else {
                    rdf_EmptyPropertyElement(xMPMetaImpl, xMPNode, node, z);
                }
            }
            return;
        }
        throw new XMPException("Invalid property element name", 202);
    }

    private static void rdf_PropertyElementList(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (!isWhitespaceNode(item)) {
                if (item.getNodeType() != (short) 1) {
                    throw new XMPException("Expected property element node not found", 202);
                }
                rdf_PropertyElement(xMPMetaImpl, xMPNode, item, z);
            }
        }
    }

    static void rdf_RDF(XMPMetaImpl xMPMetaImpl, Node node) throws XMPException {
        if (node.hasAttributes()) {
            rdf_NodeElementList(xMPMetaImpl, xMPMetaImpl.getRoot(), node);
            return;
        }
        throw new XMPException("Invalid attributes of rdf:RDF element", 202);
    }

    private static void rdf_ResourcePropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        if (!z || !"iX:changes".equals(node.getNodeName())) {
            int i;
            XMPNode addChildNode = addChildNode(xMPMetaImpl, xMPNode, node, "", z);
            for (i = 0; i < node.getAttributes().getLength(); i++) {
                Node item = node.getAttributes().item(i);
                if (!("xmlns".equals(item.getPrefix()) || (item.getPrefix() == null && "xmlns".equals(item.getNodeName())))) {
                    String localName = item.getLocalName();
                    String namespaceURI = item.getNamespaceURI();
                    if ("xml:lang".equals(item.getNodeName())) {
                        addQualifierNode(addChildNode, "xml:lang", item.getNodeValue());
                    } else if (!"ID".equals(localName) || !"http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceURI)) {
                        throw new XMPException("Invalid attribute for resource property element", 202);
                    }
                }
            }
            Object obj = null;
            for (i = 0; i < node.getChildNodes().getLength(); i++) {
                Node item2 = node.getChildNodes().item(i);
                if (!isWhitespaceNode(item2)) {
                    if (item2.getNodeType() == (short) 1 && obj == null) {
                        boolean equals = "http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(item2.getNamespaceURI());
                        String localName2 = item2.getLocalName();
                        if (equals && "Bag".equals(localName2)) {
                            addChildNode.getOptions().setArray(true);
                        } else if (equals && "Seq".equals(localName2)) {
                            addChildNode.getOptions().setArray(true).setArrayOrdered(true);
                        } else if (equals && "Alt".equals(localName2)) {
                            addChildNode.getOptions().setArray(true).setArrayOrdered(true).setArrayAlternate(true);
                        } else {
                            addChildNode.getOptions().setStruct(true);
                            if (!(equals || "Description".equals(localName2))) {
                                String namespaceURI2 = item2.getNamespaceURI();
                                if (namespaceURI2 == null) {
                                    throw new XMPException("All XML elements must be in a namespace", 203);
                                }
                                addQualifierNode(addChildNode, "rdf:type", namespaceURI2 + ':' + localName2);
                            }
                        }
                        rdf_NodeElement(xMPMetaImpl, addChildNode, item2, false);
                        if (addChildNode.getHasValueChild()) {
                            fixupQualifiedNode(addChildNode);
                        } else if (addChildNode.getOptions().isArrayAlternate()) {
                            XMPNodeUtils.detectAltText(addChildNode);
                        }
                        obj = 1;
                    } else if (obj != null) {
                        throw new XMPException("Invalid child of resource property element", 202);
                    } else {
                        throw new XMPException("Children of resource property element must be XML elements", 202);
                    }
                }
            }
            if (obj == null) {
                throw new XMPException("Missing child of resource property element", 202);
            }
        }
    }
}
