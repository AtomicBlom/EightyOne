using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Linq;
using Windows.UI.Xaml;
using RoomEditor.Framework;
using RoomEditor.Model;

// The User Control item template is documented at https://go.microsoft.com/fwlink/?LinkId=234236

namespace RoomEditor.Controls
{
    public sealed partial class RoomEdit 
    {
        public RoomEdit()
        {
            InitializeComponent();
            SizeChanged += (o, e) => RecalculateSize();
        }

        private void RecalculateSize()
        {
            ItemWidth = (int)((ActualWidth - (3 * 8)) / 3);
            ItemHeight = (int)((ActualHeight - (3 * 8)) / 3);
        }


        public static readonly DependencyProperty RoomsProperty = DependencyProperty.Register(
            "Rooms", typeof(IEnumerable<Room>), typeof(RoomEdit), new PropertyMetadata(default(IEnumerable<Room>), (o, args) => ((RoomEdit)o).CreateWrappedRooms()));

        public IEnumerable<Room> Rooms
        {
            get => (IEnumerable<Room>) GetValue(RoomsProperty);
            set
            {
                SetValue(RoomsProperty, value);
                CreateWrappedRooms();
            }
        }

        public static readonly DependencyProperty IsEditableProperty = DependencyProperty.Register(
            "IsEditable", typeof(bool), typeof(RoomEdit), new PropertyMetadata(default(bool)));

        public bool IsEditable
        {
            get => (bool) GetValue(IsEditableProperty);
            set => SetValue(IsEditableProperty, value);
        }


        public static readonly DependencyProperty RoomPatchesProperty = DependencyProperty.Register(
            "RoomPatches", typeof(IEnumerable<PatchedRoom>), typeof(RoomEdit), new PropertyMetadata(default(IEnumerable<PatchedRoom>), (o, args) => ((RoomEdit)o).CreateWrappedRooms()));

        public IEnumerable<PatchedRoom> RoomPatches
        {
            get => (IEnumerable<PatchedRoom>) GetValue(RoomPatchesProperty);
            set
            {
                SetValue(RoomPatchesProperty, value);
                CreateWrappedRooms();
            }
        }

        public static readonly DependencyProperty ItemHeightProperty = DependencyProperty.Register(
            "ItemHeight", typeof(int), typeof(RoomEdit), new PropertyMetadata(default(int)));

        public int ItemHeight
        {
            get => (int) GetValue(ItemHeightProperty);
            set => SetValue(ItemHeightProperty, value);
        }

        public static readonly DependencyProperty ItemWidthProperty = DependencyProperty.Register(
            "ItemWidth", typeof(int), typeof(RoomEdit), new PropertyMetadata(default(int)));

        public int ItemWidth
        {
            get => (int) GetValue(ItemWidthProperty);
            set => SetValue(ItemWidthProperty, value);
        }

        //public ObservableCollection<WrappedRoom> WrappedRooms => new ObservableCollection<WrappedRoom>();

        private void CreateWrappedRooms()
        {
            //RecalculateSize();

            var rooms = Rooms.ToArray();
            var overrides = RoomPatches?.ToArray();
            Debug.Assert(overrides == null || rooms.Length == overrides.Length);

            var wrappedRooms = Rooms.Select((room, index) => new WrappedRoom(room, overrides?[index])).ToList();
            Items?.Items?.Clear();
            foreach (var wrappedRoom in wrappedRooms)
            {
                Items?.Items?.Add(wrappedRoom);
            }
        }
    }

    public class WrappedRoom : BindableBase
    {
        private readonly Room _room;
        private readonly PatchedRoom _roomPatch;

        public WrappedRoom(Room room, PatchedRoom roomPatch)
        {
            _room = room;
            _roomPatch = roomPatch;
        }

        public bool IsPatched => _roomPatch?.Present != null;

        public bool IsPresent
        {
            get => _roomPatch == null ? _room.Present : _roomPatch?.Present ?? _room.Present;
            set
            {
                if (_roomPatch != null)
                {
                    if (_roomPatch.Present != value)
                    {
                        _roomPatch.Present = value;
                        RaisePropertyChanged(nameof(IsPresent));
                    }
                }
                else
                {
                    if (_room.Present != value)
                    {
                        _room.Present = value;
                        RaisePropertyChanged(nameof(IsPresent));
                    }
                }
            }
        }
    }
}
