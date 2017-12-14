package com.adobe.xmp.impl;

import com.adobe.xmp.XMPDateTime;
import com.adobe.xmp.XMPDateTimeFactory;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPUtils;
import com.adobe.xmp.impl.xpath.XMPPath;
import com.adobe.xmp.impl.xpath.XMPPathSegment;
import com.adobe.xmp.options.PropertyOptions;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class XMPNodeUtils {
    static final /* synthetic */ boolean -assertionsDisabled = (!XMPNodeUtils.class.desiredAssertionStatus());

    private XMPNodeUtils() {
    }

    static void appendLangItem(XMPNode xMPNode, String str, String str2) throws XMPException {
        XMPNode xMPNode2 = new XMPNode("[]", str2, null);
        XMPNode xMPNode3 = new XMPNode("xml:lang", str, null);
        xMPNode2.addQualifier(xMPNode3);
        if ("x-default".equals(xMPNode3.getValue())) {
            xMPNode.addChild(1, xMPNode2);
        } else {
            xMPNode.addChild(xMPNode2);
        }
    }

    static Object[] chooseLocalizedText(XMPNode xMPNode, String str, String str2) throws XMPException {
        if (!xMPNode.getOptions().isArrayAltText()) {
            throw new XMPException("Localized text array is not alt-text", 102);
        } else if (xMPNode.hasChildren()) {
            int i = 0;
            Object obj = null;
            Object obj2 = null;
            Iterator iterateChildren = xMPNode.iterateChildren();
            while (iterateChildren.hasNext()) {
                XMPNode xMPNode2 = (XMPNode) iterateChildren.next();
                if (xMPNode2.getOptions().isCompositeProperty()) {
                    throw new XMPException("Alt-text array item is not simple", 102);
                } else if (xMPNode2.hasQualifier() && "xml:lang".equals(xMPNode2.getQualifier(1).getName())) {
                    String value = xMPNode2.getQualifier(1).getValue();
                    if (str2.equals(value)) {
                        return new Object[]{new Integer(1), xMPNode2};
                    } else if (str != null && value.startsWith(str)) {
                        if (obj == null) {
                            obj = xMPNode2;
                        }
                        i++;
                    } else if ("x-default".equals(value)) {
                        obj2 = xMPNode2;
                    }
                } else {
                    throw new XMPException("Alt-text array item has no language qualifier", 102);
                }
            }
            if (i == 1) {
                return new Object[]{new Integer(2), obj};
            } else if (i > 1) {
                return new Object[]{new Integer(3), obj};
            } else if (obj2 != null) {
                return new Object[]{new Integer(4), obj2};
            } else {
                return new Object[]{new Integer(5), xMPNode.getChild(1)};
            }
        } else {
            return new Object[]{new Integer(0), null};
        }
    }

    static void deleteNode(XMPNode xMPNode) {
        XMPNode parent = xMPNode.getParent();
        if (xMPNode.getOptions().isQualifier()) {
            parent.removeQualifier(xMPNode);
        } else {
            parent.removeChild(xMPNode);
        }
        if (!parent.hasChildren() && parent.getOptions().isSchemaNode()) {
            parent.getParent().removeChild(parent);
        }
    }

    static void detectAltText(XMPNode xMPNode) {
        if (xMPNode.getOptions().isArrayAlternate() && xMPNode.hasChildren()) {
            Object obj = null;
            Iterator iterateChildren = xMPNode.iterateChildren();
            while (iterateChildren.hasNext()) {
                if (((XMPNode) iterateChildren.next()).getOptions().getHasLanguage()) {
                    obj = 1;
                    break;
                }
            }
            if (obj != null) {
                xMPNode.getOptions().setArrayAltText(true);
                normalizeLangArray(xMPNode);
            }
        }
    }

    static XMPNode findChildNode(XMPNode xMPNode, String str, boolean z) throws XMPException {
        boolean z2 = true;
        if (!(xMPNode.getOptions().isSchemaNode() || xMPNode.getOptions().isStruct())) {
            if (!xMPNode.isImplicit()) {
                throw new XMPException("Named children only allowed for schemas and structs", 102);
            } else if (xMPNode.getOptions().isArray()) {
                throw new XMPException("Named children not allowed for arrays", 102);
            } else if (z) {
                xMPNode.getOptions().setStruct(true);
            }
        }
        XMPNode findChildByName = xMPNode.findChildByName(str);
        if (findChildByName == null && z) {
            findChildByName = new XMPNode(str, new PropertyOptions());
            findChildByName.setImplicit(true);
            xMPNode.addChild(findChildByName);
        }
        if (!-assertionsDisabled) {
            if (findChildByName == null && z) {
                z2 = false;
            }
            if (!z2) {
                throw new AssertionError();
            }
        }
        return findChildByName;
    }

    private static int findIndexedItem(XMPNode xMPNode, String str, boolean z) throws XMPException {
        try {
            int parseInt = Integer.parseInt(str.substring(1, str.length() - 1));
            if (parseInt < 1) {
                throw new XMPException("Array index must be larger than zero", 102);
            }
            if (z && parseInt == xMPNode.getChildrenLength() + 1) {
                XMPNode xMPNode2 = new XMPNode("[]", null);
                xMPNode2.setImplicit(true);
                xMPNode.addChild(xMPNode2);
            }
            return parseInt;
        } catch (NumberFormatException e) {
            throw new XMPException("Array index not digits.", 102);
        }
    }

    static XMPNode findNode(XMPNode xMPNode, XMPPath xMPPath, boolean z, PropertyOptions propertyOptions) throws XMPException {
        if (xMPPath == null || xMPPath.size() == 0) {
            throw new XMPException("Empty XMPPath", 102);
        }
        XMPNode xMPNode2 = null;
        XMPNode findSchemaNode = findSchemaNode(xMPNode, xMPPath.getSegment(0).getName(), z);
        if (findSchemaNode == null) {
            return null;
        }
        if (findSchemaNode.isImplicit()) {
            findSchemaNode.setImplicit(false);
            xMPNode2 = findSchemaNode;
        }
        int i = 1;
        while (i < xMPPath.size()) {
            try {
                findSchemaNode = followXPathStep(findSchemaNode, xMPPath.getSegment(i), z);
                if (findSchemaNode == null) {
                    if (z) {
                        deleteNode(xMPNode2);
                    }
                    return null;
                }
                if (findSchemaNode.isImplicit()) {
                    findSchemaNode.setImplicit(false);
                    if (i == 1 && xMPPath.getSegment(i).isAlias() && xMPPath.getSegment(i).getAliasForm() != 0) {
                        findSchemaNode.getOptions().setOption(xMPPath.getSegment(i).getAliasForm(), true);
                    } else if (i < xMPPath.size() - 1 && xMPPath.getSegment(i).getKind() == 1 && !findSchemaNode.getOptions().isCompositeProperty()) {
                        findSchemaNode.getOptions().setStruct(true);
                    }
                    if (xMPNode2 == null) {
                        xMPNode2 = findSchemaNode;
                    }
                }
                i++;
            } catch (XMPException e) {
                if (xMPNode2 != null) {
                    deleteNode(xMPNode2);
                }
                throw e;
            }
        }
        if (xMPNode2 != null) {
            findSchemaNode.getOptions().mergeWith(propertyOptions);
            findSchemaNode.setOptions(findSchemaNode.getOptions());
        }
        return findSchemaNode;
    }

    private static XMPNode findQualifierNode(XMPNode xMPNode, String str, boolean z) throws XMPException {
        if (!-assertionsDisabled) {
            if (!(!str.startsWith("?"))) {
                throw new AssertionError();
            }
        }
        XMPNode findQualifierByName = xMPNode.findQualifierByName(str);
        if (findQualifierByName != null || !z) {
            return findQualifierByName;
        }
        findQualifierByName = new XMPNode(str, null);
        findQualifierByName.setImplicit(true);
        xMPNode.addQualifier(findQualifierByName);
        return findQualifierByName;
    }

    static XMPNode findSchemaNode(XMPNode xMPNode, String str, String str2, boolean z) throws XMPException {
        boolean z2 = false;
        if (!-assertionsDisabled) {
            if (xMPNode.getParent() == null) {
                z2 = true;
            }
            if (!z2) {
                throw new AssertionError();
            }
        }
        XMPNode findChildByName = xMPNode.findChildByName(str);
        if (findChildByName == null && z) {
            findChildByName = new XMPNode(str, new PropertyOptions().setSchemaNode(true));
            findChildByName.setImplicit(true);
            String namespacePrefix = XMPMetaFactory.getSchemaRegistry().getNamespacePrefix(str);
            if (namespacePrefix == null) {
                if (str2 == null || str2.length() == 0) {
                    throw new XMPException("Unregistered schema namespace URI", 101);
                }
                namespacePrefix = XMPMetaFactory.getSchemaRegistry().registerNamespace(str, str2);
            }
            findChildByName.setValue(namespacePrefix);
            xMPNode.addChild(findChildByName);
        }
        return findChildByName;
    }

    static XMPNode findSchemaNode(XMPNode xMPNode, String str, boolean z) throws XMPException {
        return findSchemaNode(xMPNode, str, null, z);
    }

    private static XMPNode followXPathStep(XMPNode xMPNode, XMPPathSegment xMPPathSegment, boolean z) throws XMPException {
        int kind = xMPPathSegment.getKind();
        if (kind == 1) {
            return findChildNode(xMPNode, xMPPathSegment.getName(), z);
        }
        if (kind == 2) {
            return findQualifierNode(xMPNode, xMPPathSegment.getName().substring(1), z);
        }
        if (xMPNode.getOptions().isArray()) {
            int findIndexedItem;
            if (kind == 3) {
                findIndexedItem = findIndexedItem(xMPNode, xMPPathSegment.getName(), z);
            } else if (kind == 4) {
                findIndexedItem = xMPNode.getChildrenLength();
            } else if (kind == 6) {
                r6 = Utils.splitNameAndValue(xMPPathSegment.getName());
                findIndexedItem = lookupFieldSelector(xMPNode, r6[0], r6[1]);
            } else if (kind == 5) {
                r6 = Utils.splitNameAndValue(xMPPathSegment.getName());
                findIndexedItem = lookupQualSelector(xMPNode, r6[0], r6[1], xMPPathSegment.getAliasForm());
            } else {
                throw new XMPException("Unknown array indexing step in FollowXPathStep", 9);
            }
            return (1 > findIndexedItem || findIndexedItem > xMPNode.getChildrenLength()) ? null : xMPNode.getChild(findIndexedItem);
        } else {
            throw new XMPException("Indexing applied to non-array", 102);
        }
    }

    private static int lookupFieldSelector(XMPNode xMPNode, String str, String str2) throws XMPException {
        int i = -1;
        int i2 = 1;
        while (i2 <= xMPNode.getChildrenLength() && i < 0) {
            XMPNode child = xMPNode.getChild(i2);
            if (child.getOptions().isStruct()) {
                for (int i3 = 1; i3 <= child.getChildrenLength(); i3++) {
                    XMPNode child2 = child.getChild(i3);
                    if (str.equals(child2.getName()) && str2.equals(child2.getValue())) {
                        i = i2;
                        break;
                    }
                }
                i2++;
            } else {
                throw new XMPException("Field selector must be used on array of struct", 102);
            }
        }
        return i;
    }

    static int lookupLanguageItem(XMPNode xMPNode, String str) throws XMPException {
        if (xMPNode.getOptions().isArray()) {
            for (int i = 1; i <= xMPNode.getChildrenLength(); i++) {
                XMPNode child = xMPNode.getChild(i);
                if (child.hasQualifier() && "xml:lang".equals(child.getQualifier(1).getName()) && str.equals(child.getQualifier(1).getValue())) {
                    return i;
                }
            }
            return -1;
        }
        throw new XMPException("Language item must be used on array", 102);
    }

    private static int lookupQualSelector(XMPNode xMPNode, String str, String str2, int i) throws XMPException {
        int lookupLanguageItem;
        if ("xml:lang".equals(str)) {
            lookupLanguageItem = lookupLanguageItem(xMPNode, Utils.normalizeLangValue(str2));
            if (lookupLanguageItem >= 0 || (i & 4096) <= 0) {
                return lookupLanguageItem;
            }
            XMPNode xMPNode2 = new XMPNode("[]", null);
            xMPNode2.addQualifier(new XMPNode("xml:lang", "x-default", null));
            xMPNode.addChild(1, xMPNode2);
            return 1;
        }
        for (lookupLanguageItem = 1; lookupLanguageItem < xMPNode.getChildrenLength(); lookupLanguageItem++) {
            Iterator iterateQualifier = xMPNode.getChild(lookupLanguageItem).iterateQualifier();
            while (iterateQualifier.hasNext()) {
                XMPNode xMPNode3 = (XMPNode) iterateQualifier.next();
                if (str.equals(xMPNode3.getName()) && str2.equals(xMPNode3.getValue())) {
                    return lookupLanguageItem;
                }
            }
        }
        return -1;
    }

    static void normalizeLangArray(XMPNode xMPNode) {
        if (xMPNode.getOptions().isArrayAltText()) {
            for (int i = 2; i <= xMPNode.getChildrenLength(); i++) {
                XMPNode child = xMPNode.getChild(i);
                if (child.hasQualifier() && "x-default".equals(child.getQualifier(1).getValue())) {
                    try {
                        xMPNode.removeChild(i);
                        xMPNode.addChild(1, child);
                    } catch (XMPException e) {
                        if (!-assertionsDisabled) {
                            throw new AssertionError();
                        }
                    }
                    if (i == 2) {
                        xMPNode.getChild(2).setValue(child.getValue());
                    }
                }
            }
        }
    }

    static String serializeNodeValue(Object obj) {
        String convertFromBoolean = obj == null ? null : obj instanceof Boolean ? XMPUtils.convertFromBoolean(((Boolean) obj).booleanValue()) : obj instanceof Integer ? XMPUtils.convertFromInteger(((Integer) obj).intValue()) : obj instanceof Long ? XMPUtils.convertFromLong(((Long) obj).longValue()) : obj instanceof Double ? XMPUtils.convertFromDouble(((Double) obj).doubleValue()) : obj instanceof XMPDateTime ? XMPUtils.convertFromDate((XMPDateTime) obj) : obj instanceof GregorianCalendar ? XMPUtils.convertFromDate(XMPDateTimeFactory.createFromCalendar((GregorianCalendar) obj)) : obj instanceof byte[] ? XMPUtils.encodeBase64((byte[]) obj) : obj.toString();
        return convertFromBoolean != null ? Utils.removeControlChars(convertFromBoolean) : null;
    }

    static void setNodeValue(XMPNode xMPNode, Object obj) {
        String serializeNodeValue = serializeNodeValue(obj);
        if (xMPNode.getOptions().isQualifier() ? "xml:lang".equals(xMPNode.getName()) : false) {
            xMPNode.setValue(Utils.normalizeLangValue(serializeNodeValue));
        } else {
            xMPNode.setValue(serializeNodeValue);
        }
    }

    static PropertyOptions verifySetOptions(PropertyOptions propertyOptions, Object obj) throws XMPException {
        if (propertyOptions == null) {
            propertyOptions = new PropertyOptions();
        }
        if (propertyOptions.isArrayAltText()) {
            propertyOptions.setArrayAlternate(true);
        }
        if (propertyOptions.isArrayAlternate()) {
            propertyOptions.setArrayOrdered(true);
        }
        if (propertyOptions.isArrayOrdered()) {
            propertyOptions.setArray(true);
        }
        if (!propertyOptions.isCompositeProperty() || obj == null || obj.toString().length() <= 0) {
            propertyOptions.assertConsistency(propertyOptions.getOptions());
            return propertyOptions;
        }
        throw new XMPException("Structs and arrays can't have values", 103);
    }
}
