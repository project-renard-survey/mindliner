/*
 * SearchPanel.java
 *
 * Created on 6. Juli 2007, 15:57
 */
package com.mindliner.main;

import com.mindliner.analysis.CurrentWorkTask;
import com.mindliner.analysis.MlClassHandler;
import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import com.mindliner.analysis.QueryTypes.StandardQueryType;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcRatingComparator;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.common.ObjectCheckBox;
import com.mindliner.contentfilter.BaseFilter.SortingMode;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import com.mindliner.contentfilter.mlFilterTO;
import com.mindliner.entities.Colorizer;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.gui.tablemanager.MlObjectTable;
import com.mindliner.gui.tablemanager.TableManager;
import com.mindliner.styles.MlStyler;
import com.mindliner.prefs.SearchPreferences;
import com.mindliner.thread.SearchWorker;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * This class handle provides the gui elements for general searching. It follows
 * the Singleton pattern.
 *
 * @author Marius Messerli
 */
public class SearchPanel extends JPanel {

    public static final int MAX_SEARCH_RESULTS = 100;
    public static final int MIN_ISLAND_SIZE = 3;
    public static final int SHOW_ALL_CONFIDENTIALITIES = -1;
    private static final String SEARCH_PANEL_TABLE_IDENTIFYER = "searchPanelTable";
    private final Color showAllColor = Color.black;
    private final Color constraintFilterColor = Color.red.darker();
    private MlObjectButton oneThingButton;
    private volatile static SearchPanel INSTANCE = null;
    private static final List<MlObjectTable> tables = new ArrayList<>();
    private MlObjectTable searchTable = null;

    private static enum SelectorState {

        selected,
        unSelected,
        automatic
    }

    public static mlFilterTO getSearchFilter() {
        return INSTANCE.getSearchFilterL();
    }

    public static void registerTable(MlObjectTable table) {
        if (!tables.contains(table)) {
            tables.add(table);
        }
        // restrictions might still apply from last ML execution
        restrictionsChanged();
    }

    private static void restrictionsChanged() {
        boolean isRestricted = false;
        int count = INSTANCE.OwnerList.getCheckedItems().size();
        if (count < INSTANCE.OwnerList.getModel().getSize() && count > 0) {
            isRestricted = true;
        }
        count = INSTANCE.DataPools.getCheckedItems().size();
        if (count < INSTANCE.DataPools.getModel().getSize() && count > 0) {
            isRestricted = true;
        }

        if (!INSTANCE.ModificationAgeCombo.getSelectedItem().equals(TimePeriod.All)) {
            isRestricted = true;
        }
        if (!INSTANCE.FilterShowPrivate.isSelected()) {
            isRestricted = true;
        }
        for (MlObjectTable table : tables) {
            table.setIsRestricted(isRestricted);
        }
    }

    public static void clearSearchRestrictions() {
        INSTANCE.OwnerList.makeSelectionToAll(true);
        INSTANCE.OwnerList.repaint();
        INSTANCE.DataPools.makeSelectionToAll(true);
        INSTANCE.DataPools.repaint();
        INSTANCE.FilterShowArchived.setSelected(true);
        INSTANCE.FilterShowPrivate.setSelected(true);
        INSTANCE.ModificationAgeCombo.setSelectedItem(TimePeriod.All);
        restrictionsChanged();
    }

    public static void applySelectedSorting(List<mlcObject> list) {
        INSTANCE.applyDefaultSorting(list);
    }

    public static List<mlcObject> filterObjects(List<mlcObject> inputList) {
        return INSTANCE.filterList(inputList);
    }

    public static List<MlcLink> filterLinks(List<MlcLink> inputList) {
        return INSTANCE.filterLinkList(inputList);
    }

    public static boolean evaluateObject(mlcObject candidate) {
        return INSTANCE.evaluateObjectL(candidate);
    }

    public static SearchPanel getUniqueInstance() {
        synchronized (SearchPanel.class) {
            if (INSTANCE == null) {
                INSTANCE = new SearchPanel();
                INSTANCE.initComponents();
                INSTANCE.configureComponents();
            }
        }
        return INSTANCE;
    }

    /**
     * Obtain minliner objects that are candidates for synchronization with a
     * foreign data source.
     *
     * @param type The type of object to be synched
     * @param ignoreCompleted If true the synch process will not care about
     * completed items
     * @param ignoreForeign If true the synch process will ignore any foreign
     * objects
     * @return Returns the synch candiates
     */
    public static List<mlcObject> getSynchItems(MlClassHandler.MindlinerObjectType type, boolean ignoreCompleted, boolean ignoreForeign) {
        return INSTANCE.getSynchItemsL(type, ignoreCompleted, ignoreForeign);
    }

    /**
     * Obtain the root panel of the common filter user interface elements.
     *
     * @return The root panel containing all the common filter GUI elements.
     */
    public static JPanel getRootPanel() {
        return INSTANCE;
    }

    private boolean evaluateObjectL(mlcObject candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("argument candidate must not be null");
        }
        if (candidate.getConfidentiality() == null) {
            Logger.getLogger((getClass().getName())).warning(("The object with id " + candidate.getId() + ", has a confidentiality of null, removing it from cache"));
            CacheEngineStatic.removeObjectFromCache(candidate);
            return false;
        }
        if (candidate.isPrivateAccess() && !FilterShowPrivate.isSelected()) {
            return false;
        }
        if (candidate.isArchived() && !FilterShowArchived.isSelected()) {
            return false;
        }
        if (!ObjectOwnerButton.isSelected() || OwnerList.getCheckedItems().isEmpty()) {
            return true;
        }
        boolean pass = false;
        for (Object chkbox : OwnerList.getCheckedItems()) {
            ObjectCheckBox objBox = (ObjectCheckBox) chkbox;
            mlcUser user = (mlcUser) objBox.getValue();
            if (user.getId() == candidate.getOwner().getId()) {
                pass = true;
                break;
            }
        }
        if (pass == false) {
            return false;
        }

        pass = false;
        for (Object chkbox : DataPools.getCheckedItems()) {
            ObjectCheckBox objBox = (ObjectCheckBox) chkbox;
            mlcClient client = (mlcClient) objBox.getValue();
            if (client.getId() == candidate.getClient().getId()) {
                pass = true;
                break;
            }
        }
        return pass;
    }

    /**
     * @todo Design client-side filtering like server side filtering with
     * evaluators
     */
    private List<mlcObject> filterList(List<mlcObject> inputList) {
        List<mlcObject> results = new ArrayList<>();
        for (mlcObject o : inputList) {
            if (evaluateObjectL(o)) {
                results.add(o);
            }
        }
        if (results.size() <= MAX_SEARCH_RESULTS) {
            return results;
        } else {
            return results.subList(0, MAX_SEARCH_RESULTS - 1);
        }
    }

    private List<MlcLink> filterLinkList(List<MlcLink> inputList) {
        if (!LinkOwnerButton.isSelected() || OwnerList.getCheckedItems().isEmpty()) {
            return inputList;
        }
        List<Integer> ownerIds = new ArrayList<>();
        for (Object chkbox : OwnerList.getCheckedItems()) {
            ObjectCheckBox objBox = (ObjectCheckBox) chkbox;
            mlcUser user = (mlcUser) objBox.getValue();
            ownerIds.add(user.getId());
        }

        List<MlcLink> results = new ArrayList<>();
        for (MlcLink link : inputList) {
            if (ownerIds.contains(link.getOwner().getId())) {
                results.add(link);
            } else {
                System.out.println("rejecting link between holder " + link.getHolderId() + " and relative " + link.getRelativeId());
            }
        }
        return results;
    }

    private List<mlcObject> getSynchItemsL(MlClassHandler.MindlinerObjectType type, boolean ignoreCompleted, boolean ignoreForeign) {
        List<Integer> ownerIds = new ArrayList<>();
        if (ignoreForeign) {
            mlcUser user = CacheEngineStatic.getCurrentUser();
            ownerIds.add(user.getId());
        }
        mlFilterTO fto = getSearchFilterL();
        fto.setOwnerIds(ownerIds);
        fto.setMaxNumberOfElements((short) 2000);
        fto.setShowArchived(!ignoreCompleted);
        fto.setObjectType(type);
        List<mlcObject> slist = CacheEngineStatic.getPrimarySearchHits("", fto);
        return slist;
    }

    public mlFilterTO getSearchFilterL() {
        mlFilterTO fto = new mlFilterTO();
        fto.setLastLogin(CacheEngineStatic.getCurrentUser().getLastSeen());
        fto.setShowArchived(FilterShowArchived.isSelected());
        fto.setShowPrivate(FilterShowPrivate.isSelected());
        fto.setMaxModificationAge((TimePeriod) ModificationAgeCombo.getSelectedItem());
        List<Integer> selOwnerIds = getSelectedOwnerIds();
        fto.setOwnerIds(selOwnerIds);
        List<Integer> clientIds = getSelectedDataPoolIds();
        fto.setDataPoolIds(new HashSet<>(clientIds));
        fto.setDefaultSorting((SortingMode) FilterDefaultSorting.getSelectedItem());
        fto.setMaxNumberOfElements(MAX_SEARCH_RESULTS);
        fto.setIncludeFiles(IncludeFilesBox.isVisible() ? IncludeFilesBox.isSelected() : false);
        return fto;
    }

    private void configureComponents() {
        AdvancedSearchParameterPanel.setCollapsed(true);

        // default sorting
        DefaultComboBoxModel dbm = new DefaultComboBoxModel();
        dbm.addElement(SortingMode.SearchRelevance);
        dbm.addElement(SortingMode.Creation);
        dbm.addElement(SortingMode.Modification);
        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.OBJECT_RATING)) {
            dbm.addElement(SortingMode.Rating);
        }
        FilterDefaultSorting.setModel(dbm);
        FilterDefaultSorting.setSelectedIndex(0);
        if (dbm.getSize() <= 1) {
            FilterDefaultSorting.setVisible(false);
            FilterSortingLabel.setVisible(false);
        }

        FilterShowArchived.setSelected(SearchPreferences.getShowExpired());

        // the modification age combo box
        dbm = new DefaultComboBoxModel();
        dbm.addElement(TimePeriod.SinceLastLogout);
        dbm.addElement(TimePeriod.Hour);
        dbm.addElement(TimePeriod.Day);
        dbm.addElement(TimePeriod.Week);
        dbm.addElement(TimePeriod.Fortnight);
        dbm.addElement(TimePeriod.Month);
        dbm.addElement(TimePeriod.Year);
        dbm.addElement(TimePeriod.All);
        dbm.setSelectedItem(TimePeriod.All);
        ModificationAgeCombo.setModel(dbm);

        // data pool restrictions
        DefaultListModel dlm = new DefaultListModel();
        for (Integer cid : CacheEngineStatic.getCurrentUser().getClientIds()) {
            mlcClient client = CacheEngineStatic.getClient(cid);
            assert client != null : "Confidentiality must not be null";
            dlm.addElement(new ObjectCheckBox(client, true));
        }
        DataPools.setModel(dlm);

        // owner restriction
        dlm = new DefaultListModel();
        DefaultListModel dlm1 = new DefaultListModel();
        List<mlcUser> ulist = CacheEngineStatic.getUsers();
        Collections.sort(ulist, (mlcUser f1, mlcUser f2) -> f1.toString().compareTo(f2.toString()));

        for (mlcUser u : ulist) {
            if (u.isActive()) {
                dlm.addElement(new ObjectCheckBox(u, true));
                dlm1.addElement(new ObjectCheckBox(u, true));
            }
        }
        OwnerList.setModel(dlm);
        ButtonGroup ownerGroup = new ButtonGroup();
        ownerGroup.add(ObjectOwnerButton);
        ownerGroup.add(LinkOwnerButton);
        ObjectOwnerButton.setSelected(true);

        // the standard dynamic queries
        dbm = new DefaultComboBoxModel();
        for (StandardQueryType qt : StandardQueryType.values()) {
            dbm.addElement(qt);
        }
        CannedQueries.setModel(dbm);

        searchTable = TableManager.createTypeTable(
                MlClassHandler.MindlinerObjectType.Any,
                SEARCH_PANEL_TABLE_IDENTIFYER,
                Colorizer.ColorDriverAttribute.Owner);
        searchTable.setBorder(new EmptyBorder(0, 10, 0, 0));
        TablePanel.add(searchTable, BorderLayout.CENTER);

        ResourceBundle bundle = ResourceBundle.getBundle("com/mindliner/resources/SearchFilter");
        oneThingButton = new MlObjectButton(null, bundle.getString("OneThingButtonDefaultText"));
        oneThingButton.setPreferredSize(new Dimension(100, oneThingButton.getPreferredSize().height));
        oneThingButton.setTransferHandler(new MlObjectTransferHandler());
        oneThingButton.setToolTipText(bundle.getString("OneThingButtonTT"));
        oneThingButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                List<mlcObject> hits = new ArrayList<>();
                hits.add(oneThingButton.getObject());
                MlViewDispatcherImpl.getInstance().display(hits, MlObjectViewer.ViewType.Map);
            }
        });
        OneThingPanel.add(oneThingButton);

        if (!CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.FILE_INDEXING)) {
            IncludeFilesLabel.setVisible(false);
            IncludeFilesBox.setVisible(false);
        }
        loadPreferences();
        if (!CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.CUSTOMIZATION)) {
            MoreLessLabel.setVisible(false);
            clearSearchRestrictions();
        }
        colorizeComponents();

    }

    private void colorizeComponents() {
        FixedKeyColorizer fkc = (FixedKeyColorizer) ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        Color bg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND);
        Color fg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT);
        setBackground(bg);

        StandardQueriesLabel.setForeground(fg);
        RecentQueryLabel.setForeground(fg);
        MlStyler.colorizeComboBox(CannedQueries, fkc);
        MlStyler.colorizeComboBox(RecentQueries, fkc);
        MlStyler.colorizeComboBox(ModificationAgeCombo, fkc);
        MlStyler.colorizeComboBox(FilterDefaultSorting, fkc);
        MlStyler.colorizeCheckbox(FilterShowArchived, fg, bg);
        MlStyler.colorizeCheckbox(FilterShowPrivate, fg, bg);
        MoreLessLabel.setForeground(fg);
        ModificationLabel.setForeground(fg);
        FilterSortingLabel.setForeground(fg);
        IncludeFilesLabel.setForeground(fg);
        ObjectOwnerButton.setForeground(fg);
        LinkOwnerButton.setForeground(fg);
        SetAllOwnersButton.setForeground(fg);
        SetAllOwnersButton.setBackground(bg);
        ClearAllOwnersButton.setForeground(fg);
        ClearAllOwnersButton.setBackground(bg);
        ClientLabel.setForeground(fg);
        SetAllClientsButton.setForeground(fg);
        SetAllClientsButton.setBackground(bg);
        ClearAllClientsButton.setForeground(fg);
        ClearAllClientsButton.setBackground(bg);
        OwnerFilterForLabel.setForeground(fg);
        AdvancedSearchParameterPanel.getContentPane().setBackground(bg);
        DataPools.setBackground(bg);
        DataPools.setForeground(fg);
        OwnerList.setBackground(bg);
        OwnerList.setForeground(fg);

        OneThingPanel.setBackground(bg);
        OneThingLabel.setForeground(fg);
        searchTable.applyColors(fkc);
        MlStyler.colorizeButton(oneThingButton, fkc);
    }

    public JPanel getToolBarAdditionsPane() {
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        if (!AdvancedSearchParameterPanel.isCollapsed()) {
            return super.getPreferredSize();
        } else {
            return new Dimension(super.getPreferredSize().width, super.getPreferredSize().height - AdvancedSearchParameterPanel.getPreferredSize().height);
        }
    }

    /**
     * Returns true if the current filter lets expired objects pass
     *
     * @param state True means that expired objects will be shown, false means
     * they will not be shown
     */
    public static void setShowExpired(boolean state) {
        INSTANCE.FilterShowArchived.setSelected(state);
    }

    public static void storePreferences() {

        // modification age
        Object o = INSTANCE.ModificationAgeCombo.getSelectedItem();
        if (o instanceof String) {
            SearchPreferences.setMaxModificationAge(TimePeriod.All);
        } else {
            TimePeriod ma = (TimePeriod) o;
            SearchPreferences.setMaxModificationAge(ma);
        }
        SearchPreferences.setShowExpired(INSTANCE.FilterShowArchived.isSelected());
        SearchPreferences.setSearchInFiles(INSTANCE.IncludeFilesBox.isSelected());
        SearchPreferences.setShowPrivateElements(INSTANCE.FilterShowPrivate.isSelected());
        SearchPreferences.setDefaultSorting((SortingMode) INSTANCE.FilterDefaultSorting.getSelectedItem());
        SearchPreferences.setOneThing(INSTANCE.oneThingButton.getObject());
    }

    /**
     * @todo Add binding between the bean and the UI instead of setting the
     * values twice like below
     */
    private static void loadPreferences() {

        INSTANCE.FilterShowArchived.setSelected(SearchPreferences.getShowExpired());
        INSTANCE.FilterShowPrivate.setSelected(SearchPreferences.getShowPrivateElements());
        INSTANCE.FilterDefaultSorting.setSelectedItem(SearchPreferences.getDefaultSorting());
        INSTANCE.ModificationAgeCombo.setSelectedItem(SearchPreferences.getMaxModificationAge());
        INSTANCE.oneThingButton.setObject(SearchPreferences.getOneThing());
        INSTANCE.IncludeFilesBox.setSelected(SearchPreferences.isSearchInFiles());
    }

    public void applyDefaultSorting(List<mlcObject> list) {
        if (list == null) {
            throw new IllegalArgumentException("null argument not allowed here");
        }
        SortingMode sm = (SortingMode) FilterDefaultSorting.getSelectedItem();
        switch (sm) {

            case Rating:
                // o1 and o2 swapped due to reverse sorting
                list.sort((mlcObject o1, mlcObject o2) -> {
                    if (o2.getRating() > o1.getRating()) {
                        return 1;
                    } else if (o2.getRating() < o1.getRating()) {
                        return -1;
                    }
                    return 0;
                });
                Collections.sort(list, Collections.reverseOrder(new mlcRatingComparator()));
                break;

            case Modification:
                list.sort((mlcObject o1, mlcObject o2) -> o2.getModificationDate().compareTo(o1.getModificationDate()));
                break;

            case Creation:
                list.sort((mlcObject o1, mlcObject o2) -> o2.getCreationDate().compareTo(o1.getCreationDate()));
                break;

        }
    }

    private List<Integer> getSelectedOwnerIds() {
        List<ObjectCheckBox> selOwner = (List<ObjectCheckBox>) OwnerList.getCheckedItems();
        List<Integer> selOwnerIds = new ArrayList<>();

        if (!ObjectOwnerButton.isSelected() || selOwner.size() == OwnerList.getModel().getSize()) {
            return selOwnerIds; // return empty list indicating no restriction
        }

        for (ObjectCheckBox u : selOwner) {
            mlcUser user = (mlcUser) u.getValue();
            selOwnerIds.add(user.getId()); // contacts are used as owners instead of users
        }
        return selOwnerIds;
    }

    private List<Integer> getSelectedDataPoolIds() {
        List<ObjectCheckBox> pools = (List<ObjectCheckBox>) DataPools.getCheckedItems();
        List<Integer> selDataPoolIds = new ArrayList<>();

        if (pools.size() == DataPools.getModel().getSize()) {
            return selDataPoolIds; // return empty list indicating no restriction
        }

        for (ObjectCheckBox u : pools) {
            mlcClient pool = (mlcClient) u.getValue();
            selDataPoolIds.add(pool.getId()); // contacts are used as owners instead of users
        }
        return selDataPoolIds;
    }

    // adds the specified queries to the top of the recent query combo box
    public void addQuery(String query) {

        ComboBoxModel model = RecentQueries.getModel();
        boolean found = false;
        int size = model.getSize();
        for (int i = size - 1; found == false && i >= 0; i--) {
            Object element = model.getElementAt(i);
            if (element.equals(query)) {
                found = true;
            }
        }
        if (!found) {
            RecentQueries.insertItemAt(query, 0);
            RecentQueries.setSelectedIndex(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TablePanel = new javax.swing.JPanel();
        ParameterPanel = new javax.swing.JPanel();
        OneThingPanel = new javax.swing.JPanel();
        OneThingLabel = new javax.swing.JLabel();
        DefaultSearchParameterPanel = new javax.swing.JPanel();
        QueriesPanel = new javax.swing.JPanel();
        StandardQueriesLabel = new javax.swing.JLabel();
        CannedQueries = new javax.swing.JComboBox();
        RecentQueryLabel = new javax.swing.JLabel();
        RecentQueries = new javax.swing.JComboBox();
        MoreLessLabel = new javax.swing.JLabel();
        FilterShowPrivate = new javax.swing.JCheckBox();
        FilterShowArchived = new javax.swing.JCheckBox();
        AdvancedSearchParameterPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        ModificationLabel = new javax.swing.JLabel();
        ModificationAgeCombo = new javax.swing.JComboBox();
        OwnerFilterForLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        OwnerList = new com.mindliner.common.CheckBoxList();
        SetAllOwnersButton = new javax.swing.JButton();
        ClearAllOwnersButton = new javax.swing.JButton();
        FilterSortingLabel = new javax.swing.JLabel();
        FilterDefaultSorting = new javax.swing.JComboBox();
        ClientLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        DataPools = new com.mindliner.common.CheckBoxList();
        SetAllClientsButton = new javax.swing.JButton();
        ClearAllClientsButton = new javax.swing.JButton();
        IncludeFilesLabel = new javax.swing.JLabel();
        IncludeFilesBox = new javax.swing.JCheckBox();
        ObjectOwnerButton = new javax.swing.JRadioButton();
        LinkOwnerButton = new javax.swing.JRadioButton();

        setLayout(new java.awt.BorderLayout());

        TablePanel.setOpaque(false);
        TablePanel.setLayout(new java.awt.BorderLayout());

        ParameterPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ParameterPanel.setOpaque(false);
        org.jdesktop.swingx.VerticalLayout verticalLayout1 = new org.jdesktop.swingx.VerticalLayout();
        verticalLayout1.setGap(10);
        ParameterPanel.setLayout(verticalLayout1);

        OneThingPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        OneThingPanel.setOpaque(false);
        OneThingPanel.setLayout(new java.awt.BorderLayout(0, 10));

        OneThingLabel.setFont(OneThingLabel.getFont().deriveFont(OneThingLabel.getFont().getSize()+1f));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/SearchFilter"); // NOI18N
        OneThingLabel.setText(bundle.getString("OneThingLabelText")); // NOI18N
        OneThingPanel.add(OneThingLabel, java.awt.BorderLayout.WEST);

        ParameterPanel.add(OneThingPanel);

        DefaultSearchParameterPanel.setOpaque(false);

        QueriesPanel.setOpaque(false);

        StandardQueriesLabel.setFont(StandardQueriesLabel.getFont().deriveFont(StandardQueriesLabel.getFont().getSize()+1f));
        StandardQueriesLabel.setText(bundle.getString("SearchPanelStandardQueriesLabel")); // NOI18N

        CannedQueries.setFont(CannedQueries.getFont().deriveFont(CannedQueries.getFont().getSize()+1f));
        CannedQueries.setToolTipText(bundle.getString("SearchPanelStandardQueries_TT")); // NOI18N
        CannedQueries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CannedQueriesActionPerformed(evt);
            }
        });

        RecentQueryLabel.setText(bundle.getString("SearchFilterRecentQueriesLabel")); // NOI18N

        RecentQueries.setFont(RecentQueries.getFont().deriveFont(RecentQueries.getFont().getSize()+1f));
        RecentQueries.setToolTipText(bundle.getString("SearchPanelRecentQueries_TT")); // NOI18N
        RecentQueries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RecentQueriesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout QueriesPanelLayout = new javax.swing.GroupLayout(QueriesPanel);
        QueriesPanel.setLayout(QueriesPanelLayout);
        QueriesPanelLayout.setHorizontalGroup(
            QueriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(QueriesPanelLayout.createSequentialGroup()
                .addGroup(QueriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, QueriesPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(StandardQueriesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CannedQueries, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(QueriesPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(RecentQueryLabel)
                        .addGap(8, 8, 8)
                        .addComponent(RecentQueries, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        QueriesPanelLayout.setVerticalGroup(
            QueriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(QueriesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(QueriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CannedQueries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(StandardQueriesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(QueriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RecentQueries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RecentQueryLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        MoreLessLabel.setFont(MoreLessLabel.getFont());
        MoreLessLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        MoreLessLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/bullet_triangle_glass_grey.png"))); // NOI18N
        MoreLessLabel.setToolTipText(bundle.getString("ShowAdvancedSearchOptions_TT")); // NOI18N
        MoreLessLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        MoreLessLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MoreLessLabelMouseClicked(evt);
            }
        });

        FilterShowPrivate.setSelected(true);
        FilterShowPrivate.setText(bundle.getString("FilterIncludePrivate")); // NOI18N
        FilterShowPrivate.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        FilterShowPrivate.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        FilterShowPrivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FilterShowPrivateActionPerformed(evt);
            }
        });

        FilterShowArchived.setText(bundle.getString("SearchFilterFindArchived")); // NOI18N
        FilterShowArchived.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        FilterShowArchived.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        javax.swing.GroupLayout DefaultSearchParameterPanelLayout = new javax.swing.GroupLayout(DefaultSearchParameterPanel);
        DefaultSearchParameterPanel.setLayout(DefaultSearchParameterPanelLayout);
        DefaultSearchParameterPanelLayout.setHorizontalGroup(
            DefaultSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DefaultSearchParameterPanelLayout.createSequentialGroup()
                .addGroup(DefaultSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DefaultSearchParameterPanelLayout.createSequentialGroup()
                        .addComponent(QueriesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(DefaultSearchParameterPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(FilterShowPrivate)
                        .addGap(18, 18, 18)
                        .addComponent(FilterShowArchived)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                        .addComponent(MoreLessLabel)))
                .addContainerGap())
        );
        DefaultSearchParameterPanelLayout.setVerticalGroup(
            DefaultSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DefaultSearchParameterPanelLayout.createSequentialGroup()
                .addComponent(QueriesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(DefaultSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(DefaultSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(FilterShowPrivate)
                        .addComponent(FilterShowArchived))
                    .addComponent(MoreLessLabel))
                .addContainerGap())
        );

        ParameterPanel.add(DefaultSearchParameterPanel);

        AdvancedSearchParameterPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        AdvancedSearchParameterPanel.setOpaque(false);

        ModificationLabel.setFont(ModificationLabel.getFont().deriveFont(ModificationLabel.getFont().getSize()+1f));
        ModificationLabel.setText(bundle.getString("SearchFilterMaxModificationAge")); // NOI18N

        ModificationAgeCombo.setToolTipText(bundle.getString("SearchFilterModificationAge_TT")); // NOI18N
        ModificationAgeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ModificationAgeComboActionPerformed(evt);
            }
        });

        OwnerFilterForLabel.setFont(OwnerFilterForLabel.getFont().deriveFont(OwnerFilterForLabel.getFont().getSize()+1f));
        OwnerFilterForLabel.setText(bundle.getString("OwnerSelectionTargetLabel")); // NOI18N

        OwnerList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        OwnerList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                OwnerListFocusLost(evt);
            }
        });
        jScrollPane1.setViewportView(OwnerList);

        SetAllOwnersButton.setFont(SetAllOwnersButton.getFont().deriveFont(SetAllOwnersButton.getFont().getSize()+1f));
        SetAllOwnersButton.setText(bundle.getString("OwnerSelectionSelectAllButton")); // NOI18N
        SetAllOwnersButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        SetAllOwnersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetAllOwnersButtonActionPerformed(evt);
            }
        });

        ClearAllOwnersButton.setFont(ClearAllOwnersButton.getFont().deriveFont(ClearAllOwnersButton.getFont().getSize()+1f));
        ClearAllOwnersButton.setText(bundle.getString("ObjectOwnerDeselectAllButton")); // NOI18N
        ClearAllOwnersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearAllOwnersButtonActionPerformed(evt);
            }
        });

        FilterSortingLabel.setFont(FilterSortingLabel.getFont().deriveFont(FilterSortingLabel.getFont().getSize()+1f));
        FilterSortingLabel.setLabelFor(FilterDefaultSorting);
        FilterSortingLabel.setText(bundle.getString("FilterDialogDefaultSortingLabel")); // NOI18N

        FilterDefaultSorting.setToolTipText(bundle.getString("FilterDefaultSortingCombo_TT")); // NOI18N

        ClientLabel.setFont(ClientLabel.getFont().deriveFont(ClientLabel.getFont().getSize()+1f));
        ClientLabel.setText(bundle.getString("SearchPanelClientLabel")); // NOI18N

        DataPools.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        DataPools.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                DataPoolsFocusLost(evt);
            }
        });
        jScrollPane3.setViewportView(DataPools);

        SetAllClientsButton.setFont(SetAllClientsButton.getFont().deriveFont(SetAllClientsButton.getFont().getSize()+1f));
        SetAllClientsButton.setText(bundle.getString("DataPoolSelectionSelectAllButton")); // NOI18N
        SetAllClientsButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        SetAllClientsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetAllClientsButtonActionPerformed(evt);
            }
        });

        ClearAllClientsButton.setFont(ClearAllClientsButton.getFont().deriveFont(ClearAllClientsButton.getFont().getSize()+1f));
        ClearAllClientsButton.setText(bundle.getString("DataPoolSelectionDeselectAllButton")); // NOI18N
        ClearAllClientsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearAllClientsButtonActionPerformed(evt);
            }
        });

        IncludeFilesLabel.setFont(IncludeFilesLabel.getFont().deriveFont(IncludeFilesLabel.getFont().getSize()+1f));
        IncludeFilesLabel.setText(bundle.getString("SearchInFilesCheckboxLabel")); // NOI18N

        IncludeFilesBox.setToolTipText(bundle.getString("SearchPanelSearchInFiles_TT")); // NOI18N

        ObjectOwnerButton.setFont(ObjectOwnerButton.getFont().deriveFont(ObjectOwnerButton.getFont().getSize()+1f));
        ObjectOwnerButton.setText(bundle.getString("SearchPanelObjectOwnerButton")); // NOI18N

        LinkOwnerButton.setFont(LinkOwnerButton.getFont().deriveFont(LinkOwnerButton.getFont().getSize()+1f));
        LinkOwnerButton.setText(bundle.getString("SearchPanelLinkOwnerButton")); // NOI18N

        javax.swing.GroupLayout AdvancedSearchParameterPanelLayout = new javax.swing.GroupLayout(AdvancedSearchParameterPanel.getContentPane());
        AdvancedSearchParameterPanel.getContentPane().setLayout(AdvancedSearchParameterPanelLayout);
        AdvancedSearchParameterPanelLayout.setHorizontalGroup(
            AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdvancedSearchParameterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(AdvancedSearchParameterPanelLayout.createSequentialGroup()
                        .addComponent(SetAllClientsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ClearAllClientsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ClientLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(AdvancedSearchParameterPanelLayout.createSequentialGroup()
                        .addComponent(SetAllOwnersButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ClearAllOwnersButton, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(AdvancedSearchParameterPanelLayout.createSequentialGroup()
                        .addComponent(OwnerFilterForLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ObjectOwnerButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(LinkOwnerButton))
                    .addGroup(AdvancedSearchParameterPanelLayout.createSequentialGroup()
                        .addGroup(AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(IncludeFilesLabel)
                            .addComponent(ModificationLabel)
                            .addComponent(FilterSortingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ModificationAgeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(AdvancedSearchParameterPanelLayout.createSequentialGroup()
                                .addComponent(IncludeFilesBox)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(FilterDefaultSorting, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        AdvancedSearchParameterPanelLayout.setVerticalGroup(
            AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdvancedSearchParameterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ModificationLabel)
                    .addComponent(ModificationAgeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FilterSortingLabel)
                    .addComponent(FilterDefaultSorting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(IncludeFilesLabel)
                    .addComponent(IncludeFilesBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(OwnerFilterForLabel)
                    .addComponent(ObjectOwnerButton)
                    .addComponent(LinkOwnerButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ClearAllOwnersButton)
                    .addComponent(SetAllOwnersButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ClientLabel)
                .addGap(1, 1, 1)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AdvancedSearchParameterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SetAllClientsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ClearAllClientsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        ParameterPanel.add(AdvancedSearchParameterPanel);

        TablePanel.add(ParameterPanel, java.awt.BorderLayout.NORTH);

        add(TablePanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void OwnerListMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_OwnerListMouseExited
        restrictionsChanged();
    }//GEN-LAST:event_OwnerListMouseExited

    private void FilterShowPrivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FilterShowPrivateActionPerformed
        restrictionsChanged();
    }//GEN-LAST:event_FilterShowPrivateActionPerformed

    private void MoreLessLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MoreLessLabelMouseClicked
        if (AdvancedSearchParameterPanel.isCollapsed()) {
            AdvancedSearchParameterPanel.setCollapsed(false);
        } else {
            AdvancedSearchParameterPanel.setCollapsed(true);
        }
    }//GEN-LAST:event_MoreLessLabelMouseClicked

    private void ClearAllOwnersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearAllOwnersButtonActionPerformed
        OwnerList.makeSelectionToAll(false);
        restrictionsChanged();
        OwnerList.repaint();
    }//GEN-LAST:event_ClearAllOwnersButtonActionPerformed

    private void SetAllOwnersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetAllOwnersButtonActionPerformed
        OwnerList.makeSelectionToAll(true);
        restrictionsChanged();
        OwnerList.repaint();
    }//GEN-LAST:event_SetAllOwnersButtonActionPerformed

    private void OwnerListFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_OwnerListFocusLost
        restrictionsChanged();
    }//GEN-LAST:event_OwnerListFocusLost

    private void ModificationAgeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ModificationAgeComboActionPerformed
        if (ModificationAgeCombo.getSelectedItem().equals(TimePeriod.All)) {
            ModificationAgeCombo.setForeground(showAllColor);
        } else {
            ModificationAgeCombo.setForeground(constraintFilterColor);
        }
        restrictionsChanged();
    }//GEN-LAST:event_ModificationAgeComboActionPerformed

    private void DataPoolsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_DataPoolsFocusLost
        restrictionsChanged();
    }//GEN-LAST:event_DataPoolsFocusLost

    private void SetAllClientsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetAllClientsButtonActionPerformed
        DataPools.makeSelectionToAll(true);
        restrictionsChanged();
        DataPools.repaint();
    }//GEN-LAST:event_SetAllClientsButtonActionPerformed

    private void ClearAllClientsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearAllClientsButtonActionPerformed
        DataPools.makeSelectionToAll(false);
        restrictionsChanged();
        DataPools.repaint();
    }//GEN-LAST:event_ClearAllClientsButtonActionPerformed

    private void CannedQueriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CannedQueriesActionPerformed
        List<mlcObject> results = new ArrayList();
        if (CannedQueries.getSelectedItem() instanceof String) {
            // should perhaps clear the list here....
        } else {
            switch ((StandardQueryType) CannedQueries.getSelectedItem()) {
                case RecentChanges:
                    results = CacheEngineStatic.getPrimarySearchHits("", getSearchFilterL());
                    break;

                case OverdueTasks:
                    results = (List) CacheEngineStatic.getMyOverdueTasks();
                    break;

                case UpcomingTasks:
                    results = (List) CacheEngineStatic.getMyUpcomingTasks(TimePeriod.Fortnight);
                    break;

                case PriorityTasks:
                    results = (List) CacheEngineStatic.getMyPriorityTasks();
                    break;

                case CurrentWorkTasks:
                    List<CurrentWorkTask> currentWorkTasks = CacheEngineStatic.getCurrentWorkTasks();
                    if (!currentWorkTasks.isEmpty()) {
                        for (CurrentWorkTask cwt : currentWorkTasks) {
                            mlcTask task = (mlcTask) CacheEngineStatic.getObject(cwt.getTaskId());
                            if (task != null) {
                                results.add(task);
                            }
                        }
                    }
                    break;

                case StandAloneObjects:
                    results = (List) CacheEngineStatic.getStandAloneObjects();
                    break;

                case IslandPeaks:
                    results = CacheEngineStatic.getIslandPeaks(SOMEBITS, MAX_SEARCH_RESULTS);
                    break;
            }
        }
        MlViewDispatcherImpl.getInstance().display(results, MlObjectViewer.ViewType.GenericTable);
        searchTable.setSearchStringOnly("");
    }//GEN-LAST:event_CannedQueriesActionPerformed

    private void RecentQueriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RecentQueriesActionPerformed
        RecentQueries.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SearchWorker sw = new SearchWorker(RecentQueries.getSelectedItem().toString(), MindlinerObjectType.Any, MlObjectViewer.ViewType.GenericTable);
        sw.execute();
        searchTable.setSearchStringOnly(RecentQueries.getSelectedItem().toString());
        RecentQueries.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

    }//GEN-LAST:event_RecentQueriesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXCollapsiblePane AdvancedSearchParameterPanel;
    private javax.swing.JComboBox CannedQueries;
    private javax.swing.JButton ClearAllClientsButton;
    private javax.swing.JButton ClearAllOwnersButton;
    private javax.swing.JLabel ClientLabel;
    private com.mindliner.common.CheckBoxList DataPools;
    private javax.swing.JPanel DefaultSearchParameterPanel;
    private javax.swing.JComboBox FilterDefaultSorting;
    private javax.swing.JCheckBox FilterShowArchived;
    private javax.swing.JCheckBox FilterShowPrivate;
    private javax.swing.JLabel FilterSortingLabel;
    private javax.swing.JCheckBox IncludeFilesBox;
    private javax.swing.JLabel IncludeFilesLabel;
    private javax.swing.JRadioButton LinkOwnerButton;
    private javax.swing.JComboBox ModificationAgeCombo;
    private javax.swing.JLabel ModificationLabel;
    private javax.swing.JLabel MoreLessLabel;
    private javax.swing.JRadioButton ObjectOwnerButton;
    private javax.swing.JLabel OneThingLabel;
    private javax.swing.JPanel OneThingPanel;
    private javax.swing.JLabel OwnerFilterForLabel;
    private com.mindliner.common.CheckBoxList OwnerList;
    private javax.swing.JPanel ParameterPanel;
    private javax.swing.JPanel QueriesPanel;
    private javax.swing.JComboBox RecentQueries;
    private javax.swing.JLabel RecentQueryLabel;
    private javax.swing.JButton SetAllClientsButton;
    private javax.swing.JButton SetAllOwnersButton;
    private javax.swing.JLabel StandardQueriesLabel;
    private javax.swing.JPanel TablePanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables

}