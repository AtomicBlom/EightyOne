using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.ApplicationModel;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using RoomEditor.Model;
using RoomEditor.ViewModel;

// The Blank Page item template is documented at https://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace RoomEditor
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        public MainPage()
        {
            this.InitializeComponent();
            if (!DesignMode.DesignModeEnabled)
            {
                DataContext = new ViewModel.RoomEditorViewModel();
            }
        }

        public RoomEditorViewModel ViewModel
        {
            get { return (RoomEditorViewModel) DataContext; }
            set { DataContext = value; }
        }


        private void ListViewBase_OnItemClick(object sender, ItemClickEventArgs e)
        {
            if (DataContext is ViewModel.RoomEditorViewModel v)
            {
                v.CurrentRoomSet = (RoomSet) e.ClickedItem;
            }
        }
    }
}
