package com.adobe.xmp.impl;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.impl.xpath.XMPPathParser;
import com.adobe.xmp.options.PropertyOptions;
import java.util.Iterator;

public class XMPMetaImpl implements XMPMeta {
    static final /* synthetic */ boolean -assertionsDisabled = (!XMPMetaImpl.class.desiredAssertionStatus());
    private String packetHeader;
    private XMPNode tree;

    public XMPMetaImpl() {
        this.packetHeader = null;
        this.tree = new XMPNode(null, null, null);
    }

    public XMPMetaImpl(XMPNode xMPNode) {
        this.packetHeader = null;
        this.tree = xMPNode;
    }

    public Object clone() {
        return new XMPMetaImpl((XMPNode) this.tree.clone());
    }

    public boolean doesPropertyExist(String str, String str2) {
        boolean z = false;
        try {
            ParameterAsserts.assertSchemaNS(str);
            ParameterAsserts.assertPropName(str2);
            if (XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), false, null) != null) {
                z = true;
            }
            return z;
        } catch (XMPException e) {
            return false;
        }
    }

    public XMPNode getRoot() {
        return this.tree;
    }

    public void setLocalizedText(String str, String str2, String str3, String str4, String str5, PropertyOptions propertyOptions) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertArrayName(str2);
        ParameterAsserts.assertSpecificLang(str4);
        str3 = str3 != null ? Utils.normalizeLangValue(str3) : null;
        str4 = Utils.normalizeLangValue(str4);
        XMPNode findNode = XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), true, new PropertyOptions(7680));
        if (findNode == null) {
            throw new XMPException("Failed to find or create array node", 102);
        }
        if (!findNode.getOptions().isArrayAltText()) {
            if (findNode.hasChildren() || !findNode.getOptions().isArrayAlternate()) {
                throw new XMPException("Specified property is no alt-text array", 102);
            }
            findNode.getOptions().setArrayAltText(true);
        }
        Object obj = null;
        XMPNode xMPNode = null;
        Iterator iterateChildren = findNode.iterateChildren();
        while (iterateChildren.hasNext()) {
            XMPNode xMPNode2 = (XMPNode) iterateChildren.next();
            if (xMPNode2.hasQualifier() && "xml:lang".equals(xMPNode2.getQualifier(1).getName())) {
                if ("x-default".equals(xMPNode2.getQualifier(1).getValue())) {
                    xMPNode = xMPNode2;
                    obj = 1;
                    break;
                }
            }
            throw new XMPException("Language qualifier must be first", 102);
        }
        if (xMPNode != null && findNode.getChildrenLength() > 1) {
            findNode.removeChild(xMPNode);
            findNode.addChild(1, xMPNode);
        }
        Object[] chooseLocalizedText = XMPNodeUtils.chooseLocalizedText(findNode, str3, str4);
        int intValue = ((Integer) chooseLocalizedText[0]).intValue();
        XMPNode xMPNode3 = (XMPNode) chooseLocalizedText[1];
        boolean equals = "x-default".equals(str4);
        switch (intValue) {
            case 0:
                XMPNodeUtils.appendLangItem(findNode, "x-default", str5);
                obj = 1;
                if (!equals) {
                    XMPNodeUtils.appendLangItem(findNode, str4, str5);
                    break;
                }
                break;
            case 1:
                if (!equals) {
                    if (!(obj == null || xMPNode == xMPNode3 || xMPNode == null || !xMPNode.getValue().equals(xMPNode3.getValue()))) {
                        xMPNode.setValue(str5);
                    }
                    xMPNode3.setValue(str5);
                    break;
                }
                if (!-assertionsDisabled) {
                    Object obj2 = (obj == null || xMPNode != xMPNode3) ? null : 1;
                    if (obj2 == null) {
                        throw new AssertionError();
                    }
                }
                iterateChildren = findNode.iterateChildren();
                while (iterateChildren.hasNext()) {
                    xMPNode2 = (XMPNode) iterateChildren.next();
                    if (xMPNode2 != xMPNode) {
                        if (xMPNode2.getValue().equals(xMPNode != null ? xMPNode.getValue() : null)) {
                            xMPNode2.setValue(str5);
                        }
                    }
                }
                if (xMPNode != null) {
                    xMPNode.setValue(str5);
                    break;
                }
                break;
            case 2:
                if (!(obj == null || xMPNode == xMPNode3 || xMPNode == null || !xMPNode.getValue().equals(xMPNode3.getValue()))) {
                    xMPNode.setValue(str5);
                }
                xMPNode3.setValue(str5);
                break;
            case 3:
                XMPNodeUtils.appendLangItem(findNode, str4, str5);
                if (equals) {
                    obj = 1;
                    break;
                }
                break;
            case 4:
                if (xMPNode != null && findNode.getChildrenLength() == 1) {
                    xMPNode.setValue(str5);
                }
                XMPNodeUtils.appendLangItem(findNode, str4, str5);
                break;
            case 5:
                XMPNodeUtils.appendLangItem(findNode, str4, str5);
                if (equals) {
                    obj = 1;
                    break;
                }
                break;
            default:
                throw new XMPException("Unexpected result from ChooseLocalizedText", 9);
        }
        if (obj == null && findNode.getChildrenLength() == 1) {
            XMPNodeUtils.appendLangItem(findNode, "x-default", str5);
        }
    }

    void setNode(XMPNode xMPNode, Object obj, PropertyOptions propertyOptions, boolean z) throws XMPException {
        if (z) {
            xMPNode.clear();
        }
        xMPNode.getOptions().mergeWith(propertyOptions);
        if (!xMPNode.getOptions().isCompositeProperty()) {
            XMPNodeUtils.setNodeValue(xMPNode, obj);
        } else if (obj == null || obj.toString().length() <= 0) {
            xMPNode.removeChildren();
        } else {
            throw new XMPException("Composite nodes can't have values", 102);
        }
    }

    public void setPacketHeader(String str) {
        this.packetHeader = str;
    }

    public void setProperty(String str, String str2, Object obj) throws XMPException {
        setProperty(str, str2, obj, null);
    }

    public void setProperty(String str, String str2, Object obj, PropertyOptions propertyOptions) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertPropName(str2);
        propertyOptions = XMPNodeUtils.verifySetOptions(propertyOptions, obj);
        XMPNode findNode = XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), true, propertyOptions);
        if (findNode != null) {
            setNode(findNode, obj, propertyOptions, false);
            return;
        }
        throw new XMPException("Specified property does not exist", 102);
    }

    public void sort() {
        this.tree.sort();
    }
}
