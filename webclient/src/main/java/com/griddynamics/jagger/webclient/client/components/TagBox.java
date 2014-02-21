package com.griddynamics.jagger.webclient.client.components;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import com.griddynamics.jagger.webclient.client.dto.TagDto;
import com.griddynamics.jagger.webclient.client.resources.JaggerResources;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.*;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.dnd.core.client.GridDropTarget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;


public class TagBox extends AbstractWindow implements IsWidget {

    private SessionComparisonPanel.TreeItem currentTreeItem;

    private TextArea descriptionPanel;
    private Grid<TagDto> gridStorageL;
    private Grid<TagDto> gridStorageR;

    private ListStore<TagDto> storeFrom;
    private ListStore<TagDto> storeTo;


    private List<TagDto> sessionTagsDB;

    private TextButton allRight, right, left, allLeft;
    private VerticalPanel buttonBar;


    private final int PIXELS_BETWEEN_BUTTONS = 10;
    private final String DEFAULT_TITLE = "Click on any row...";

    private VerticalPanel vp;
    private TextButton saveButton;
    private TextButton cancelButton;
    private TreeGrid<SessionComparisonPanel.TreeItem> treeGrid;


    interface TagDtoProperties extends PropertyAccess<TagDto> {
        @Path("name")
        ModelKeyProvider<TagDto> name();

        @Path("name")
        ValueProvider<TagDto, String> descriptionProp();

    }

    public TagBox() {
        super();
        TagDtoProperties props = GWT.create(TagDtoProperties.class);
        storeFrom = new ListStore<TagDto>(props.name());
        storeTo = new ListStore<TagDto>(props.name());

       // handleSetTagDto();
        descriptionPanel = new TextArea();
        descriptionPanel.setReadOnly(true);
        descriptionPanel.setStyleName(JaggerResources.INSTANCE.css().descriptionPanel());
        descriptionPanel.setPixelSize(width, 70);

        vp = new VerticalPanel();
        vp.setPixelSize(width, height);

        saveButton = new TextButton("Save");
        saveButton.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                String tags = "";
                for (int i = 0; i < storeTo.size(); i++) {
                    tags += storeTo.get(i).getName() + " ";

                }
                currentTreeItem.put(getText(), tags);
                treeGrid.getTreeView().refresh(false);
                descriptionPanel.setText(DEFAULT_TITLE);
                saveTagToDataBase();

                gridStorageL.getStore().clear();
                gridStorageR.getStore().clear();
                hide();
            }
        });

        saveButton.setPixelSize(60, 22);
        saveButton.getElement().setMargins(new Margins(0, 0, 0, 0));

        cancelButton = new TextButton("Cancel");
        cancelButton.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                gridStorageL.getStore().clear();
                gridStorageR.getStore().clear();
                descriptionPanel.setText(DEFAULT_TITLE);
                hide();
            }
        });


        cancelButton.setPixelSize(60, 22);
        cancelButton.getElement().setMargins(new Margins(0, 0, 0, PIXELS_BETWEEN_BUTTONS));

        buttonBar = new VerticalPanel();
        buttonBar.getElement().getStyle().setProperty("margin", "1px");
        buttonBar.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        buttonBar.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        buttonBar.setVisible(true);


        allRight = new TextButton(">>>");
        allRight.setPixelSize(40, 15);
        allRight.addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                  onAllRight();
                descriptionPanel.setText(DEFAULT_TITLE);
            }
        });


        right = new TextButton(">");
        right.setPixelSize(40, 15);
        right.addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                onRight();
            }
        });


        left = new TextButton("<");
        left.setPixelSize(40, 15);
        left.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                 onLeft();
            }
        });


        allLeft = new TextButton("<<<");
        allLeft.setPixelSize(40, 15);
        allLeft.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                   onAllLeft();
                   descriptionPanel.setText(DEFAULT_TITLE);
            }
        });


        buttonBar.setPixelSize(100, 150);
        buttonBar.add(allRight);
        buttonBar.add(right);
        buttonBar.add(left);
        buttonBar.add(allLeft);

        DockPanel dp = new DockPanel();
        dp.setPixelSize(width, 50);
        dp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        dp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(5);
        hp.add(saveButton);
        hp.add(cancelButton);
        dp.setSpacing(5);
        dp.add(hp, DockPanel.EAST);
        dp.add(new Label(""), DockPanel.CENTER);

        DockPanel gridsAndButtons = new DockPanel();
        gridsAndButtons.setPixelSize(width,390);




        gridStorageL = new Grid<TagDto>(storeFrom, createColumnList(props, "Available tags"));
        gridStorageL.setBorders(true);
        gridStorageL.getView().setForceFit(true);


        gridStorageR = new Grid<TagDto>(storeTo, createColumnList(props, "Session's tags"));
        gridStorageR.setBorders(true);
        gridStorageR.getView().setForceFit(true);

        new GridDragSource<TagDto>(gridStorageL);
        new GridDragSource<TagDto>(gridStorageR);

        new GridDropTarget<TagDto>(gridStorageL).addDropHandler(new DndDropEvent.DndDropHandler() {
            @Override
            public void onDrop(DndDropEvent dndDropEvent) {
                descriptionPanel.setText(DEFAULT_TITLE);
            }
        });
        new GridDropTarget<TagDto>(gridStorageR).addDropHandler(new DndDropEvent.DndDropHandler() {
            @Override
            public void onDrop(DndDropEvent dndDropEvent) {
                descriptionPanel.setText(DEFAULT_TITLE);
            }
        });


        gridStorageL.setPixelSize(250, 380);
        gridStorageR.setPixelSize(250, 380);
        gridsAndButtons.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        gridsAndButtons.add(gridStorageL,DockPanel.WEST);
        gridsAndButtons.add(buttonBar,DockPanel.CENTER);
        gridsAndButtons.add(gridStorageR,DockPanel.EAST);

        gridStorageL.addRowMouseDownHandler(new RowMouseDownEvent.RowMouseDownHandler() {
            @Override
            public void onRowMouseDown(RowMouseDownEvent rowMouseDownEvent) {
                descriptionPanel.setText(gridStorageL.getStore().get(rowMouseDownEvent.getRowIndex()).getDescription());
                gridStorageR.getSelectionModel().deselectAll();
            }
        });

        gridStorageR.addRowMouseDownHandler(new RowMouseDownEvent.RowMouseDownHandler() {
            @Override
            public void onRowMouseDown(RowMouseDownEvent rowMouseDownEvent) {
                descriptionPanel.setText(gridStorageR.getStore().get(rowMouseDownEvent.getRowIndex()).getDescription());
                gridStorageL.getSelectionModel().deselectAll();

            }
        });


        if (gridStorageR.getSelectionModel().getSelectedItems().isEmpty() ||
            gridStorageL.getSelectionModel().getSelectedItems().isEmpty())
            descriptionPanel.setText(DEFAULT_TITLE);

        VerticalPanel descriptionShell = new VerticalPanel();

        descriptionShell.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        descriptionShell.add(descriptionPanel);
        descriptionShell.setPixelSize(width,30);
        descriptionPanel.setReadOnly(true);
        vp.add(gridsAndButtons);
        vp.add(descriptionShell);
        vp.add(dp);
        setAutoHideEnabled(true);
        addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                gridStorageL.getStore().clear();
                gridStorageR.getStore().clear();
                descriptionPanel.setText(DEFAULT_TITLE);
                hide();
            }
        });
        add(vp);

    }


    public void setTreeGrid(TreeGrid<SessionComparisonPanel.TreeItem> treeGrid) {
        this.treeGrid = treeGrid;
    }


    void popUp(String sessionId, SessionComparisonPanel.TreeItem item, List<TagDto> allTags, List<TagDto> sessionTags) {
        setText(sessionId);
        setGrids(allTags, sessionTags);
        currentTreeItem = item;
        show();
    }

    private ColumnModel<TagDto> createColumnList(TagDtoProperties props, String columnName) {
        ColumnConfig<TagDto, String> cc1 = new ColumnConfig<TagDto, String>(props.descriptionProp());
        cc1.setHeader(SafeHtmlUtils.fromString(columnName));
        cc1.setFixed(true);
        cc1.setMenuDisabled(true);
        cc1.setWidth(250);
        List<ColumnConfig<TagDto, ?>> l = new ArrayList<ColumnConfig<TagDto, ?>>();
        l.add(cc1);
        return new ColumnModel<TagDto>(l);
    }


    private void onRight() {
        List<TagDto> selectedList =gridStorageL.getSelectionModel().getSelectedItems();
        gridStorageL.getSelectionModel().selectNext(false);
        descriptionPanel.setText(gridStorageL.getSelectionModel().getSelectedItem().getDescription());

        gridStorageR.getSelectionModel().deselectAll();
        gridStorageR.getStore().addAll(selectedList);

        for (int i=0; i<selectedList.size(); i++){
            gridStorageL.getStore().remove(selectedList.get(i));
        }

    }

    private void onLeft() {
        List<TagDto> selectedList =gridStorageR.getSelectionModel().getSelectedItems();
        gridStorageR.getSelectionModel().selectNext(false);
        descriptionPanel.setText(gridStorageR.getSelectionModel().getSelectedItem().getDescription());

        gridStorageL.getSelectionModel().deselectAll();
        gridStorageL.getStore().addAll(selectedList);

        for (int i=0; i<selectedList.size(); i++){
            gridStorageR.getStore().remove(selectedList.get(i));
        }
    }

    private void onAllRight(){
        gridStorageR.getStore().addAll(gridStorageL.getStore().getAll());
        gridStorageL.getStore().clear();

    }

    private void onAllLeft(){
        gridStorageL.getStore().addAll(gridStorageR.getStore().getAll());
        gridStorageR.getStore().clear();

    }

    public void setGrids(List<TagDto> allTags, List<TagDto> sessionTags){

        gridStorageL.getStore().addAll(allTags);
        gridStorageR.getStore().addAll(sessionTags);
        sessionTagsDB = sessionTags;
        for (int i=0; i<gridStorageR.getStore().size(); i++){
            gridStorageL.getStore().remove(gridStorageR.getStore().get(i));
        }
    }

    public void saveTagToDataBase(){
        sessionTagsDB.clear();
        sessionTagsDB.addAll(gridStorageR.getStore().getAll());
    }

}

