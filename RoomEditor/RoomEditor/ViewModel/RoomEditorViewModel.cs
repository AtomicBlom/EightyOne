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
        private RoomSet _currentRoomSet;
        private int _nextRoomId;

        private ObservableCollection<RoomSet> _roomProfiles;
        private ObservableCollection<RoomSet> _selectedRoomProfiles;
        private RoomSet _testRoomSet;
        private RoomSetPatch _northRoomSetPatch;
        private RoomSetPatch _eastRoomSetPatch;
        private RoomSetPatch _westRoomSetPatch;
        private RoomSetPatch _southRoomSetPatch;

        public RoomEditorViewModel()
        {
            RoomProfiles = new ObservableCollection<RoomSet>();
            SelectedRoomProfiles = new ObservableCollection<RoomSet>();
            SelectedRoomProfiles.CollectionChanged += SelectedItemsOnCollectionChanged;
            AddNewRoomCommand = new DelegateCommand(ExecuteAddNewRoom);
            SaveRoomSetsCommand = new DelegateCommand(ExecuteSaveRoomSets);
            LoadRoomSetsCommand = new DelegateCommand(ExecuteLoadRoomSets);
        }

        public ICommand AddNewRoomCommand { get; set; }
        public ICommand SaveRoomSetsCommand { get; set; }
        public ICommand LoadRoomSetsCommand { get; set; }

        public ObservableCollection<RoomSet> RoomProfiles
        {
            get => _roomProfiles;
            private set => SetField(ref _roomProfiles, value);
        }

        public ObservableCollection<RoomSet> SelectedRoomProfiles
        {
            get => _selectedRoomProfiles;
            set => SetField(ref _selectedRoomProfiles, value);
        }

        public RoomSetPatch NorthRoomSetPatch
        {
            get => _northRoomSetPatch;
            set {
                if (SetField(ref _northRoomSetPatch, value))
                {
                    UpdateRoomConnections();
                }
            }
        }

        public RoomSetPatch EastRoomSetPatch
        {
            get => _eastRoomSetPatch;
            set
            {
                if (SetField(ref _eastRoomSetPatch, value))
                {
                    UpdateRoomConnections();
                }
            }
        }

        public RoomSetPatch WestRoomSetPatch
        {
            get => _westRoomSetPatch;
            set
            {
                if (SetField(ref _westRoomSetPatch, value))
                {
                    UpdateRoomConnections();
                }
            }
        }

        public RoomSetPatch SouthRoomSetPatch
        {
            get => _southRoomSetPatch;
            set
            {
                if (SetField(ref _southRoomSetPatch, value))
                {
                    UpdateRoomConnections();
                }
            }
        }

        public RoomSet CurrentRoomSet
        {
            get => _currentRoomSet;
            set
            {
                var _previousValue = _currentRoomSet;
                if (SetField(ref _currentRoomSet, value))
                {
                    if (_previousValue != null)
                    {
                        foreach (var roomProfileRoom in _previousValue.Rooms)
                            roomProfileRoom.PropertyChanged -= OnRoomChanged;
                    }
                    if (_currentRoomSet != null)
                    {
                        foreach (var roomProfileRoom in _currentRoomSet.Rooms)
                            roomProfileRoom.PropertyChanged += OnRoomChanged;
                    }

                    if (TestRoomSet == null)
                    {
                        TestRoomSet = value;
                    }
                    UpdateRoomConnections();
                }
            }
        }

        public RoomSet TestRoomSet
        {
            get => _testRoomSet;
            set
            {
                if (SetField(ref _testRoomSet, value))
                    UpdateRoomConnections();
            }
        }

        public IEnumerable<bool> NorthSideConnections => Enumerable.Range(0, RoomSize)
            .Select(i => IsRoomEnabled(CurrentRoomSet, i, 0) && IsRoomEnabled(TestRoomSet, i, RoomSize - 1));

        public bool IsNorthSideValid => NorthSideConnections.Any(_ => _);

        public IEnumerable<bool> SouthSideConnections => Enumerable.Range(0, RoomSize)
            .Select(i => IsRoomEnabled(CurrentRoomSet, i, RoomSize - 1) && IsRoomEnabled(TestRoomSet, i, 0));

        public bool IsSouthSideValid => SouthSideConnections.Any(_ => _);

        public IEnumerable<bool> EastSideConnections => Enumerable.Range(0, RoomSize)
            .Select(i => IsRoomEnabled(CurrentRoomSet, RoomSize - 1, i) && IsRoomEnabled(TestRoomSet, 0, i));

        public bool IsEastSideValid => EastSideConnections.Any(_ => _);

        public IEnumerable<bool> WestSideConnections => Enumerable.Range(0, RoomSize)
            .Select(i => IsRoomEnabled(CurrentRoomSet, 0, i) && IsRoomEnabled(TestRoomSet, RoomSize - 1, i));

        public bool IsWestSideValid => WestSideConnections.Any(_ => _);

        private void SelectedItemsOnCollectionChanged(object sender, NotifyCollectionChangedEventArgs notifyCollectionChangedEventArgs)
        {
            CurrentRoomSet = notifyCollectionChangedEventArgs.NewItems.OfType<RoomSet>().FirstOrDefault();
        }

        private void ExecuteAddNewRoom()
        {
            var roomId = _nextRoomId++;
            var roomProfile = new RoomSet
            {
                Id = roomId,
                Name = $"New Room {roomId}",
                Rooms = Enumerable.Range(0, RoomSize * RoomSize)
                    .Select(value => new Room(value % RoomSize, value / RoomSize, false))
                    .ToArray()
            };

            RoomProfiles.Add(roomProfile);
            CurrentRoomSet = roomProfile;
            if (RoomProfiles.Count == 1)
            {
                TestRoomSet = RoomProfiles.Single();
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

        private bool IsRoomEnabled(RoomSet roomSet, int x, int z)
        {
            return roomSet?.Rooms[z * RoomSize + x].Present ?? false;
        }
    }
}