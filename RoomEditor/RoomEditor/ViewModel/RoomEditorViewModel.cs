using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Windows.Input;
using RoomEditor.Framework;
using RoomEditor.Model;
using RoomEditor.Services;

namespace RoomEditor.ViewModel
{
    public class RoomEditorViewModel : BindableBase
    {
        protected const int RoomSize = 3;
        private RoomProfile _currentRoomProfile;
        private int _nextRoomId;

        private ObservableCollection<RoomProfile> _roomProfiles;
        private ObservableCollection<RoomProfile> _selectedRoomProfiles;
        private RoomProfile _testRoomProfile;

        public RoomEditorViewModel()
        {
            RoomProfiles = new ObservableCollection<RoomProfile>();
            SelectedRoomProfiles = new ObservableCollection<RoomProfile>();
            SelectedRoomProfiles.CollectionChanged += SelectedItemsOnCollectionChanged;
            AddNewRoomCommand = new DelegateCommand(ExecuteAddNewRoom);
            SaveRoomSetsCommand = new DelegateCommand(ExecuteSaveRoomSets);
            LoadRoomSetsCommand = new DelegateCommand(ExecuteLoadRoomSets);
        }

        public ICommand AddNewRoomCommand { get; set; }
        public ICommand SaveRoomSetsCommand { get; set; }
        public ICommand LoadRoomSetsCommand { get; set; }

        public ObservableCollection<RoomProfile> RoomProfiles
        {
            get => _roomProfiles;
            private set => SetField(ref _roomProfiles, value);
        }

        public ObservableCollection<RoomProfile> SelectedRoomProfiles
        {
            get => _selectedRoomProfiles;
            set => SetField(ref _selectedRoomProfiles, value);
        }

        public RoomProfile CurrentRoomProfile
        {
            get => _currentRoomProfile;
            set
            {
                if (SetField(ref _currentRoomProfile, value))
                    UpdateRoomConnections();
            }
        }

        public RoomProfile TestRoomProfile
        {
            get => _testRoomProfile;
            set
            {
                if (SetField(ref _testRoomProfile, value))
                    UpdateRoomConnections();
            }
        }

        public IEnumerable<bool> NorthSideConnections => Enumerable.Range(0, RoomSize)
            .Select(i => IsRoomEnabled(CurrentRoomProfile, i, 0) && IsRoomEnabled(TestRoomProfile, i, RoomSize - 1));

        public bool IsNorthSideValid => NorthSideConnections.Any(_ => _);

        public IEnumerable<bool> SouthSideConnections => Enumerable.Range(0, RoomSize)
            .Select(i => IsRoomEnabled(CurrentRoomProfile, i, RoomSize - 1) && IsRoomEnabled(TestRoomProfile, i, 0));

        public bool IsSouthSideValid => SouthSideConnections.Any(_ => _);

        public IEnumerable<bool> EastSideConnections => Enumerable.Range(0, RoomSize)
            .Select(i => IsRoomEnabled(CurrentRoomProfile, RoomSize - 1, i) && IsRoomEnabled(TestRoomProfile, 0, i));

        public bool IsEastSideValid => EastSideConnections.Any(_ => _);

        public IEnumerable<bool> WestSideConnections => Enumerable.Range(0, RoomSize)
            .Select(i => IsRoomEnabled(CurrentRoomProfile, 0, i) && IsRoomEnabled(TestRoomProfile, RoomSize - 1, i));

        public bool IsWestSideValid => WestSideConnections.Any(_ => _);

        private void SelectedItemsOnCollectionChanged(object sender, NotifyCollectionChangedEventArgs notifyCollectionChangedEventArgs)
        {
            CurrentRoomProfile = notifyCollectionChangedEventArgs.NewItems.OfType<RoomProfile>().FirstOrDefault();
        }

        private void ExecuteAddNewRoom()
        {
            var roomId = _nextRoomId++;
            var roomProfile = new RoomProfile
            {
                Id = roomId,
                Name = $"New Room {roomId}",
                Rooms = Enumerable.Range(0, RoomSize * RoomSize)
                    .Select(value => new EditableRoom(value % RoomSize, value / RoomSize, false))
                    .ToArray()
            };

            foreach (var roomProfileRoom in roomProfile.Rooms)
                roomProfileRoom.PropertyChanged += OnRoomChanged;

            RoomProfiles.Add(roomProfile);
            CurrentRoomProfile = roomProfile;
            if (RoomProfiles.Count == 1)
            {
                TestRoomProfile = RoomProfiles.Single();
            }
        }

        private async void ExecuteLoadRoomSets()
        {
            try
            {
                var fileService = new FileService();
                RoomProfiles.Clear();
                foreach (var roomProfile in await fileService.LoadRooms())
                {
                    RoomProfiles.Add(roomProfile);
                }
            }
            catch (Exception e)
            {
                Debug.WriteLine("Error Loading file {0}", e);
            }
        }

        private async void ExecuteSaveRoomSets()
        {
            try
            {
                var fileService = new FileService();
                await fileService.SaveRooms(RoomSize, RoomSize, RoomProfiles);
            }
            catch (Exception e)
            {
                Debug.WriteLine("Error Saving file {0}", e);
            }
        }

        private void OnRoomChanged(object sender, PropertyChangedEventArgs e)
        {
            UpdateRoomConnections();
        }

        private void UpdateRoomConnections()
        {
            RaisePropertyChanged(nameof(NorthSideConnections));
            RaisePropertyChanged(nameof(SouthSideConnections));
            RaisePropertyChanged(nameof(EastSideConnections));
            RaisePropertyChanged(nameof(WestSideConnections));
            RaisePropertyChanged(nameof(IsNorthSideValid));
            RaisePropertyChanged(nameof(IsSouthSideValid));
            RaisePropertyChanged(nameof(IsEastSideValid));
            RaisePropertyChanged(nameof(IsWestSideValid));
        }

        private bool IsRoomEnabled(RoomProfile roomProfile, int x, int z)
        {
            return roomProfile?.Rooms[z * RoomSize + x].Present ?? false;
        }
    }
}