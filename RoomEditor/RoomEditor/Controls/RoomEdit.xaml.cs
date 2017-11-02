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
        }


        public static readonly DependencyProperty RoomsProperty = DependencyProperty.Register(
            "Rooms", typeof(IEnumerable<Room>), typeof(RoomEdit), new PropertyMetadata(default(IEnumerable<Room>)));

        public IEnumerable<Room> Rooms
        {
            get => (IEnumerable<Room>) GetValue(RoomsProperty);
            set
            {
                SetValue(RoomsProperty, value);
                CreateWrappedRooms();
            }
        }

        public static readonly DependencyProperty RoomPatchesProperty = DependencyProperty.Register(
            "RoomPatches", typeof(IEnumerable<PatchedRoom>), typeof(RoomEdit), new PropertyMetadata(default(IEnumerable<PatchedRoom>)));

        public IEnumerable<PatchedRoom> RoomPatches
        {
            get => (IEnumerable<PatchedRoom>) GetValue(RoomPatchesProperty);
            set
            {
                SetValue(RoomPatchesProperty, value);
                CreateWrappedRooms();
            }
        }

        private int ItemWidth => (int) ((ActualWidth - (3 * 4)) / 3);
        private int ItemHeight => (int)((ActualHeight - (3 * 4)) / 3);

        private ObservableCollection<WrappedRoom> WrappedRooms => new ObservableCollection<WrappedRoom>();

        private void CreateWrappedRooms()
        {
            var rooms = Rooms.ToArray();
            var overrides = RoomPatches?.ToArray();
            Debug.Assert(overrides == null || rooms.Length == overrides.Length);

            WrappedRooms.Clear();
            for (var i = 0; i < rooms.Length; i++)
            {
                WrappedRooms.Add(new WrappedRoom(rooms[i], overrides?[i]));
            }
        }
    }

    internal class WrappedRoom : BindableBase
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
